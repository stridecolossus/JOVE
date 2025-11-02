package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.function.*;

/**
 * A <i>transformer</i> marshals a Java type to/from the corresponding native representation.
 * @param <T> Domain type
 * @param <N> Native representation
 * @author Sarge
 */
public interface Transformer<T, N> {
    /**
     * @return Memory layout of the native type
     */
	default MemoryLayout layout() {
		return ValueLayout.ADDRESS;
	}

	/**
	 * @return Transformer for an array of this native type
	 */
	default Transformer<?, ?> array() {
		return new ArrayTransformer(this);
	}

	/**
	 * Marshals a method argument to its off-heap representation.
	 * @param arg			Argument
	 * @param allocator		Off-heap allocator
	 * @return Off-heap argument
	 * @see #empty()
	 */
	Object marshal(T arg, SegmentAllocator allocator);

	/**
	 * @return Empty native value
	 */
	default Object empty() {
		return MemorySegment.NULL;
	}

	// TODO - 'ref' parameter indicating whether being marshalled as by-reference parameter => can avoid actually marshalling if required (e.g. structures) but still does allocation

	/**
	 * @return Transformer to unmarshal a native value
	 * @throws UnsupportedOperationException if the domain type cannot be returned from a native method
	 */
	Function<N, T> unmarshal();

	/**
	 * @return Update method for a by-reference parameter
	 * @throws UnsupportedOperationException if the native type cannot be returned as a by-reference parameter
	 */
	default BiConsumer<N, T> update() {
		throw new UnsupportedOperationException();
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
