package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.function.*;

/**
 * The <i>identity transformer</i> marshals an argument as-is, i.e. without any transformation.
 * @author Sarge
 */
public record IdentityTransformer<T>(ValueLayout layout) implements Transformer<T, T> {
	/**
	 * Constructor.
	 * @param layout Memory layout
	 */
	public IdentityTransformer {
		requireNonNull(layout);
	}

	@Override
	public T marshal(T arg, SegmentAllocator allocator) {
		return arg;
	}

	@Override
	public Object empty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Function<T, T> unmarshal() {
		return Function.identity();
	}

	@Override
	public Transformer<?, ?> array() {
		return new ArrayTransformer(this) {
			@Override
			protected void marshal(Object array, int length, MemorySegment address, SegmentAllocator allocator) {
				MemorySegment.copy(array, 0, address, layout, 0, length);
			}

			@Override
			public BiConsumer<MemorySegment, Object> update() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Registers identity transformers for the built-in primitive types.
	 * @param registry Transformer registry
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void primitives(Registry registry) {
		final ValueLayout[] primitives = {
	    		JAVA_BOOLEAN,
	    		JAVA_BYTE,
	    		JAVA_CHAR,
	    		JAVA_SHORT,
	    		JAVA_INT,
	    		JAVA_LONG,
	    		JAVA_FLOAT,
	    		JAVA_DOUBLE
		};

		for(ValueLayout layout : primitives) {
    		final var transformer = new IdentityTransformer<>(layout);
			final Class carrier = layout.carrier();
    		registry.register(carrier, transformer);
    	}
    }
	// TODO - also wrappers? otherwise add doc
}
