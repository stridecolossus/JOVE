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
	 * @return Domain type
	 */
	Class<T> type();

	/**
	 * @return Native memory layout
	 */
	MemoryLayout layout();

	/**
	 * Derives a native mapper for the subclass.
	 * @param target 		Target type
	 * @param registry		Native mappers
	 * @return Derived mapper
	 */
	NativeMapper<? extends T, R> derive(Class<? extends T> target, NativeMapperRegistry registry);

	/**
	 * Marshals the given value to its native representation.
	 * @param instance		Data to marshal
	 * @param allocator		Allocator
	 */
	Object marshal(T instance, SegmentAllocator allocator);

	/**
	 * Marshals an empty value to its native representation, i.e. {@code null} for reference types.
	 * @return Empty native value
	 * @throws NullPointerException if this mapper does not support empty values
	 */
	Object empty();

	/**
	 * Helper - Marshals a value to its native representation (including {@link #empty()} values).
	 * @param arg			Value to marshal
	 * @param mapper		Native mapper
	 * @param allocator		Allocator
	 * @return Marshalled value
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	static Object marshal(Object arg, NativeMapper mapper, SegmentAllocator allocator) {
		if(arg == null) {
			return mapper.empty();
		}
		else {
			return mapper.marshal(arg, allocator);
		}
	}

	/**
	 * Provides the mapper for a native return value.
	 * @return Return value mapper
	 * @throws UnsupportedOperationException if this type cannot be returned from a native method
	 */
	Function<R, T> returns();

	/**
	 * Provides the mapper for a by-reference parameter.
	 * @return Returned parameter mapper
	 * @throws UnsupportedOperationException if this type cannot be returned as a by-reference parameter
	 * @see Returned
	 */
	BiConsumer<R, T> reference();
}
