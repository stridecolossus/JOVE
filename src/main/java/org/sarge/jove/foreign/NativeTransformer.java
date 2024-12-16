package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.*;

/**
 * A <i>native transformer</i> defines a domain type that can be transformed to/from its corresponding FFM representation.
 * <p>
 * The {@link #transform(Object, ParameterMode, SegmentAllocator)} method is responsible for allocation and population of the associated off-heap memory (if any).
 * <p>
 * A type that can be returned from a native method should implement the {@link #returns(Class)} factory method.
 * <p>
 * Similarly a type that can be returned as a <i>by reference</i> parameter implements the {@link #update()} method.
 * <p>
 * @param <T> Domain type
 * @param <R> Native type
 * @see Returned
 * @author Sarge
 */
public interface NativeTransformer<T, R> {
	/**
	 * @return Native memory layout
	 */
	default MemoryLayout layout() {
		return ValueLayout.ADDRESS;
	}

	/**
	 * The <i>parameter mode</i> specifies whether a native parameter is passed <i>by value</i> or returned <i>by reference</i> (indicated by the {@link Returned} annotation).
	 */
	enum ParameterMode {
		VALUE,
		REFERENCE
	}

	/**
	 * Transforms a domain object to its native representation and allocates off-heap memory as required.
	 * @param value			Domain value
	 * @param mode 			Parameter mode
	 * @param allocator		Allocator
	 * @return Native value
	 */
	R transform(T value, ParameterMode mode, SegmentAllocator allocator);

	/**
	 * Provides the inverse transformer for a method return value of this type.
	 * @return Return value transformer
	 * @throws UnsupportedOperationException if this type cannot be returned from a native method
	 */
	Function<R, T> returns();

	/**
	 * Provides a handler to update a by-reference argument.
	 * @return Update handler
	 * @throws UnsupportedOperationException if this type cannot be returned by-reference
	 * @see ParameterMode#REFERENCE
	 * @see Returned
	 */
	default BiConsumer<R, T> update() {
		throw new UnsupportedOperationException();
	}
}
