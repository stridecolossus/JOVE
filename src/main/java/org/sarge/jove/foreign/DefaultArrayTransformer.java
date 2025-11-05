package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.util.function.*;

import org.sarge.jove.foreign.DefaultArrayTransformer.ElementAccessor.DefaultElementAccessor;

/**
 * A <i>default array transformer</i> marshals array elements using a provided {@link ElementAccessor}.
 * @author Sarge
 */
public class DefaultArrayTransformer extends AbstractArrayTransformer {
	/**
	 * An <i>element accessor</i> is used to access and update an off-heap array element.
	 */
	protected interface ElementAccessor {
		/**
		 * Sets an off-heap element.
		 * @param address		Off-heap memory
		 * @param index			Index
		 * @param element		Element
		 */
		void set(MemorySegment address, int index, Object element);

		/**
		 * Gets an off-heap element.
		 * @param address		Off-heap memory
		 * @param index			Index
		 * @return Element
		 */
		Object get(MemorySegment address, int index);

		/**
		 * Default implementation that accesses off-heap array elements using a {@link VarHandle}.
		 */
		class DefaultElementAccessor implements ElementAccessor {
			private final VarHandle handle;

			/**
			 * Constructor.
			 * @param layout Component layout
			 */
			public DefaultElementAccessor(MemoryLayout layout) {
				this.handle = Transformer.removeOffset(layout.arrayElementVarHandle());
			}

			@Override
			public void set(MemorySegment address, int index, Object element) {
	    		handle.set(address, (long) index, element);
			}

			@Override
			public Object get(MemorySegment address, int index) {
				return handle.get(address, (long) index);
			}
		}
	}

	private final ElementAccessor accessor;

	@SuppressWarnings("rawtypes")
	private Function unmarshal;

	/**
	 * Constructor.
	 * @param component Component transformer
	 */
	public DefaultArrayTransformer(Transformer<?, ?> component) {
		final var accessor = new DefaultElementAccessor(component.layout());
		this(component, accessor);
	}

	/**
	 * Constructor.
	 * @param component			Component transformer
	 * @param accessor			Element accessor
	 */
	protected DefaultArrayTransformer(Transformer<?, ?> component, ElementAccessor accessor) {
		super(component);
		this.accessor = requireNonNull(accessor);
	}

	@Override
	protected void marshal(Object array, int length, MemorySegment address, SegmentAllocator allocator) {
		for(int n = 0; n < length; ++n) {
			// Get element
			final Object element = Array.get(array, n);

			// Skip empty elements
			if(element == null) {
    			continue;
    		}

			// Transform to native type
			@SuppressWarnings("unchecked")
			final Object result = component.marshal(element, allocator);

			// Write off-heap memory
			accessor.set(address, n, result);
    	}
	}

	@Override
	public BiConsumer<MemorySegment, Object> update() {
		return (address, array) -> {
			final int length = Array.getLength(array);
			update(address, array, length);
		};
	}

	/**
	 * Updates a by-reference array parameter.
	 * @param address		Off-heap memory
	 * @param array			Array
	 * @param length		Length
	 * @see #accessor()
	 */
	protected void update(MemorySegment address, Object array, int length) {
		// Cache unmarshalling function
		if(unmarshal == null) {
			unmarshal = component.unmarshal();
		}

		// Unmarshal array
		for(int n = 0; n < length; ++n) {
			// Retrieve off-heap element
			final Object foreign = accessor.get(address, n);

			// Skip empty elements
			if(MemorySegment.NULL.equals(foreign)) {
				continue;
			}

			// Unmarshal element
			@SuppressWarnings("unchecked")
			final Object element = unmarshal.apply(foreign);

			// Write to array
			Array.set(array, n, element);
		}
	}
}
