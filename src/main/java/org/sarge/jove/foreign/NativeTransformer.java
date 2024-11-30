package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.*;

/**
 * A <i>native transformer</i> defines a domain type that can be transformed to/from its corresponding FFM representation.
 * @param <T> Domain type
 * @param <R> Native type
 * @author Sarge
 */
public interface NativeTransformer<T, R> {
	/**
	 * @return Domain type
	 */
	Class<? extends T> type();

	/**
	 * @return Native memory layout
	 */
	MemoryLayout layout();

	/**
	 * Derives a transformer for the given target subclass.
	 * @param target Target type
	 * @return Derived transformer
	 */
	NativeTransformer<? extends T, R> derive(Class<? extends T> target);

	/**
	 * Transforms the given value to its native representation.
	 * @param instance		Data to transform
	 * @param allocator		Allocator
	 * @return Native value
	 */
	Object transform(T instance, SegmentAllocator allocator);

	// TODO - ugly, only used for structure[]
	// TODO - could it just allocate anyway and copy segment???
	void transform(T instance, MemorySegment address, SegmentAllocator allocator);

	/**
	 * Transforms an <i>empty</i> value to its native representation, i.e. {@code null}.
	 * @return Empty native value
	 * @throws NullPointerException if this transformer does not support empty values
	 */
	Object empty();

	/**
	 * Helper - Transforms a value to its native representation (including {@link #empty()} values).
	 * @param value			Value to transform
	 * @param transform		Transformer
	 * @param allocator		Allocator
	 * @return Native value
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	static Object transform(Object value, NativeTransformer transform, SegmentAllocator allocator) {
		if(value == null) {
			return transform.empty();
		}
		else {
			return transform.transform(value, allocator);
		}
	}

	/**
	 * Provides the inverse transformer for a method return value.
	 * @return Return value transformer
	 * @throws UnsupportedOperationException if the domain type cannot be returned from a native method
	 */
	Function<R, T> returns();

	/**
	 * Provides a consumer to update a by-reference parameter after method invocation.
	 * @return Returned parameter update consumer
	 * @throws UnsupportedOperationException if the domain type cannot be returned as a by-reference parameter
	 * @see Returned
	 */
	BiConsumer<R, T> update();
}
