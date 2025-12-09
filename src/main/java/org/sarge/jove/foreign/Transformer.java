package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.function.*;

/**
 * A <i>transformer</i> marshals a domain type to/from the corresponding native representation.
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
	 * Marshals a method argument to its off-heap representation.
	 * @param arg			Argument
	 * @param allocator		Off-heap allocator
	 * @return Off-heap argument
	 * @see #marshal(Object, Transformer, SegmentAllocator)
	 */
	N marshal(T arg, SegmentAllocator allocator);

	/**
	 * Helper.
	 * Marshals a possibly empty value.
	 * @param value				Value
	 * @param transformer		Transformer
	 * @param allocator			Off-heap allocator
	 * @return Value to marshal
	 * @see #empty()
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	static Object marshal(Object value, Transformer transformer, SegmentAllocator allocator) {
		if(value == null) {
			return transformer.empty();
		}
		else {
			return transformer.marshal(value, allocator);
		}
	}

	/**
	 * @return Empty native value
	 */
	default Object empty() {
		return MemorySegment.NULL;
	}

	/**
	 * Provides a function to unmarshal an off-heap return value.
	 * @return Unmarshalling function
	 * @throws UnsupportedOperationException if the domain type cannot be returned from a native method
	 */
	default Function<N, T> unmarshal() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Provides an array transformer for this native type
	 * @return Array transformer
	 */
	default AbstractArrayTransformer array() {
		return new DefaultArrayTransformer(this);
	}

	/**
	 * Provides a consumer to update a by-reference parameter after invocation.
	 * @return Update function
	 * @throws UnsupportedOperationException if the native type cannot be returned as a by-reference parameter
	 */
	default BiConsumer<MemorySegment, T> update() {
		throw new UnsupportedOperationException();
	}

    /**
     * Helper.
     * Injects a zero byte offset coordinate into the given method handle at index <b>one</b>.
     * @param handle Method handle
     * @return Method handle without an offset
     */
	static VarHandle removeOffset(VarHandle handle) {
		return MethodHandles.insertCoordinates(handle, 1, 0L);
	}
}
