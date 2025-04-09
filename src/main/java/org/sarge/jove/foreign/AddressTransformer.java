package org.sarge.jove.foreign;

import java.lang.foreign.*;

/**
 * An <i>address transformer</i> marshals a reference type to/from the native layer.
 * @param <T> Reference type
 * @param <R> Native type
 * @author Sarge
 */
public non-sealed interface AddressTransformer<T extends Object, R> extends Transformer {
    @Override
    default MemoryLayout layout() {
    	return ValueLayout.ADDRESS;
    }

    /**
	 * Marshals a reference argument to its native representation.
	 * @param arg			Reference argument
	 * @param allocator		Off-heap allocator
	 * @return Native argument
     */
    Object marshal(T arg, SegmentAllocator allocator);

    /**
	 * @return Empty or {@code null} argument
     */
    default Object empty() {
    	return MemorySegment.NULL;
    }

    /**
	 * Unmarshals a native value.
	 * @param value Native value
	 * @return Reference type
	 * @throws UnsupportedOperationException if this type cannot logically be returned from a native method
     */
    Object unmarshal(R value);
}
