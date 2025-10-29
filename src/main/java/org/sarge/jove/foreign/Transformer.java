package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.lang.invoke.*;

/**
 * A <i>transformer</i> marshals a Java type to/from the corresponding native representation.
 * @param <T> Domain type
 * @param <N> Native representation
 * @author Sarge
 */
public interface Transformer<T, N> {
    /**
     * @return Memory layout of this type
     */
	default MemoryLayout layout() {
		return ValueLayout.ADDRESS;
	}

	/**
	 * Marshals a method argument to its off-heap representation.
	 * @param arg			Argument
	 * @param allocator		Off-heap allocator
	 * @return Off-heap argument
	 */
	Object marshal(T arg, SegmentAllocator allocator);

	/**
	 * Unmarshals a value from its off-heap representation.
	 * @param value Off-heap value
	 * @return Unmarshalled value
	 * @throws UnsupportedOperationException if the value cannot be returned from a native method
	 */
	T unmarshal(N value);

	/**
	 * Transformation function for a by-reference parameter.
	 * @param <T> Returned type
	 * @see #update(MemorySegment, Object)
	 */
	interface ReturnedTransformer<T> {
		/**
		 * Updates a by-reference parameter.
		 * @param address		Off-heap memory
		 * @param argument		Parameter instance
		 */
		void update(MemorySegment address, T argument);
	}

	/**
	 * @return Update function for a by-reference parameter
	 * @throws UnsupportedOperationException if this transformer cannot return a by-reference parameter
	 */
	default ReturnedTransformer<T> update() {
		throw new UnsupportedOperationException("Native type cannot be returned by-reference");
	}

    /**
     * Helper - Inserts a zero byte offset coordinate into the given method handle at index <b>one</b>.
     * @param handle Method handle
     * @return Method handle with no byte offsets
     */
	static VarHandle removeOffset(VarHandle handle) {
		return MethodHandles.insertCoordinates(handle, 1, 0L);
	}
}
