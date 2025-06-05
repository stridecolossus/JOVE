package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * An <i>array transformer</i> marshals an array to/from off-heap memory.
 * @author Sarge
 */
public class ArrayTransformer<T> implements Transformer<T[]> {
	private final Transformer<T> transformer;

	/**
	 * Constructor.
	 * @param transformer Component transformer
	 */
	public ArrayTransformer(Transformer<T> transformer) {
		this.transformer = requireNonNull(transformer);
	}

	@Override
	public MemoryLayout layout() {
		return ValueLayout.ADDRESS;
	}

	@Override
	public MemorySegment marshal(T[] array, SegmentAllocator allocator) {
		// Allocate off-heap array
		final MemoryLayout layout = transformer.layout();
		final MemorySegment address = allocator.allocate(layout, array.length);

		// Transform elements and populate off-heap memory
		if(array instanceof NativeStructure[]) {
			marshalStructureArray(array, address, allocator);
		}
		else {
			marshalReferenceArray(array, address, allocator);
		}

		return address;
	}

	// TODO - should array element transformation be a function of the Transformer?
	// i.e. returns a int consumer function that marshals each element?

	private void marshalStructureArray(T[] array, MemorySegment address, SegmentAllocator allocator) {
		final MemoryLayout layout = transformer.layout();
		final long size = layout.byteSize();
		for(int n = 0; n < array.length; ++n) {
			final Object element = Transformer.marshal(array[n], transformer, allocator);
    		final MemorySegment slice = address.asSlice(n * size, size);
    		slice.copyFrom((MemorySegment) element);
		}
	}

	private void marshalReferenceArray(T[] array, MemorySegment address, SegmentAllocator allocator) {
		final MemoryLayout layout = transformer.layout();
    	final var handle = Transformer.removeOffset(layout.arrayElementVarHandle());
    	for(int n = 0; n < array.length; ++n) {
    		final Object element = Transformer.marshal(array[n], transformer, allocator);
    		handle.set(address, (long) n, element);
    	}
	}

	@Override
	public Function<?, T[]> unmarshal() {
		throw new UnsupportedOperationException();
	}
}
