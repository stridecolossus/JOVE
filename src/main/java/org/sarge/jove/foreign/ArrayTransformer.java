package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.util.function.*;

/**
 * An <i>array transformer</i> marshals an array to/from off-heap memory.
 * @author Sarge
 */
public class ArrayTransformer implements Transformer<Object, MemorySegment> {
	protected final Transformer transformer;

	/**
	 * Constructor.
	 * @param transformer Component transformer
	 */
	public ArrayTransformer(Transformer transformer) {
		this.transformer = requireNonNull(transformer);
	}

	@Override
	public final MemoryLayout layout() {
		return ValueLayout.ADDRESS;
	}

	/**
	 * Creates a handle to the off-heap memory of this array.
	 * @return Off-heap array handle
	 */
	protected VarHandle handle() {
		return Transformer.removeOffset(transformer.layout().arrayElementVarHandle());
	}

	@Override
	public MemorySegment marshal(Object array, SegmentAllocator allocator) {
		final MemoryLayout layout = transformer.layout();
		final int len = Array.getLength(array);
		final MemorySegment address = allocator.allocate(layout, len);
		marshal(array, len, address, allocator);
		return address;
	}

	/**
	 * Marshals an array to off-heap memory.
	 * @param array			Array
	 * @param length		Length
	 * @param address		Off-heap memory
	 * @param allocator		Allocator
	 */
	protected void marshal(Object array, int length, MemorySegment address, SegmentAllocator allocator) {
		final VarHandle handle = handle();

		// TODO
		// ObjIntConsumer<Object> consumer = (element, index) -> handle.set(address, (long) index, element);

		for(int n = 0; n < length; ++n) {
			final Object value = Array.get(array, n);

			if(value == null) {
    			continue;
    		}

    		final Object element = transformer.marshal(value, allocator);

    		handle.set(address, (long) n, element);
    	}
	}

	@Override
	public final Function<MemorySegment, Object> unmarshal() {
		throw new UnsupportedOperationException();
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
	 */
	protected void update(MemorySegment address, Object array, int length) {
		final VarHandle handle = handle();

		for(int n = 0; n < length; ++n) {
			final Object element = handle.get(address, (long) n);

			if(MemorySegment.NULL.equals(element)) {
				continue;
			}

			Array.set(array, n, transformer.unmarshal().apply(element));
		}
	}
}
