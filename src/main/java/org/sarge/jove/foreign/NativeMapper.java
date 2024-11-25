package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.*;

/**
 * A <i>native mapper</i> defines a domain type that can be marshalled to/from a corresponding native representation.
 * @param <T> Type
 * @author Sarge
 */
public interface NativeMapper<T, R> {
	/**
	 * Derives a native mapper for the native type subclass.
	 * @param target Target type
	 * @return Derived native mapper
	 */
	NativeMapper<? extends T, R> derive(Class<? extends T> target);

	/**
	 * @return Type
	 */
	Class<T> type();

	/**
	 * @return Native memory layout
	 */
	MemoryLayout layout();

	/**
	 * Marshals the given value to its native representation.
	 * @param instance		Instance to marshal
	 * @param allocator		Allocator for off-heap memory
	 * @return Native value
	 */
	Object marshal(T instance, SegmentAllocator allocator);

	/**
	 * Marshals a {@code null} value to its native representation.
	 * @return Native value
	 * @throws NullPointerException if this mapper does not support {@code null} native values
	 */
	Object marshalNull();

	/**
	 * Provides the mapper for a return value of this native type.
	 * @return Return value mapper
	 * @throws UnsupportedOperationException if this type cannot be returned from a native method
	 */
	Function<R, T> returns();

	/**
	 * Provides the mapper for a by-reference parameter of this native type.
	 * @return Returned parameter mapper
	 * @throws UnsupportedOperationException if this type cannot be returned as a by-reference parameter
	 * @see Returned
	 */
	BiConsumer<R, T> reference();
}
