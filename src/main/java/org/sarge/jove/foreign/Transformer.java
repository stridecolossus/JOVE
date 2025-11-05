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
	 * @see DefaultArrayTransformer
	 */
	default Transformer<?, ?> array() {
		return new DefaultArrayTransformer(this);
	}

	/**
	 * Marshals a method argument to its off-heap representation.
	 * @param arg			Argument
	 * @param allocator		Off-heap allocator
	 * @return Off-heap argument
	 * @see #empty()
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
	 * @return Transformer to unmarshal a native value
	 * @throws UnsupportedOperationException if the domain type cannot be returned from a native method
	 */
	Function<N, T> unmarshal();

	/**
	 * @return Update transformer for a by-reference parameter
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
