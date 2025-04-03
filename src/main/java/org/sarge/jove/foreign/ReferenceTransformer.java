package org.sarge.jove.foreign;

import java.lang.foreign.*;

/**
 * A <i>reference transformer</i> marshals a Java or domain type to/from the native layer.
 * @param <T> Java or domain type
 * @author Sarge
 */
public non-sealed interface ReferenceTransformer<T extends Object, R> extends Transformer {
    @Override
    default MemoryLayout layout() {
    	return ValueLayout.ADDRESS;
    }

    /**
	 * Marshals a Java argument to its native representation.
	 * @param arg			Java argument
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
	 * @return Java or domain value
	 * @throws UnsupportedOperationException if this type cannot logically be returned from a native method
     */
    T unmarshal(R address);
}
