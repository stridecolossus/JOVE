package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.function.Function;

/**
 * A <i>transformer</i> marshals a Java primitive or reference type to/from the corresponding native representation.
 * @param <T> Domain type
 * @author Sarge
 */
public interface Transformer<T> {
    /**
     * @return Memory layout of this type
     */
	default MemoryLayout layout() {
    	return ValueLayout.ADDRESS;
    }

    /**
	 * Marshals an argument to its native representation.
	 * @param arg			Argument
	 * @param allocator		Off-heap allocator
	 * @return Native argument
	 * @see #empty()
     */
    Object marshal(T arg, SegmentAllocator allocator);

    /**
	 * @return Empty or {@code null} argument
     */
    default Object empty() {
    	return MemorySegment.NULL;
    }

    /**
     * Marshals an argument to its native equivalent.
     * @param arg				Argument
     * @param transformer		Transformer
     * @param allocator			Allocator
     * @return Native value
     * @see #empty()
     */
	@SuppressWarnings({"rawtypes", "unchecked"})
	static Object marshal(Object arg, Transformer transformer, SegmentAllocator allocator) {
		if(arg == null) {
			return transformer.empty();
		}
		else {
			return transformer.marshal(arg, allocator);
		}
	}

    /**
     * Provides a transformer to unmarshal a native return value of this type.
	 * @return Unmarshalling function
	 * @throws UnsupportedOperationException if this type cannot logically be returned from a native method
     */
    Function<? extends Object, T> unmarshal();

    /**
     * Inserts a zero byte offset coordinate into the given method handle at index <b>one</b>.
     * @param handle Method handle
     * @return Method handle with no byte offsets
     */
	static VarHandle removeOffset(VarHandle handle) {
		return MethodHandles.insertCoordinates(handle, 1, 0L);
	}
}
