package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * A <i>native transformer</i> maps a Java argument to/from its corresponding native type.
 * @param <T> Java type
 * @author Sarge
 */
public interface NativeTransformer<T> {
	/**
	 * @return Off-heap memory layout
	 */
	MemoryLayout layout();

	/**
	 * Marshals the given argument to the corresponding native type.
	 * @param arg 			Java argument
	 * @param allocator		Native allocator
	 * @return Native value
	 * @throws IllegalArgumentException if the argument cannot be transformed
	 */
	Object marshal(T arg, SegmentAllocator allocator);

	/**
	 * Provides a function to unmarshal a native type.
	 * @return Unmarshalling function
	 * @throws UnsupportedOperationException if this type cannot logically be returned from a native method
	 */
	Function<? extends Object, T> unmarshal();

	/**
	 * A <i>native transformer factory</i> generates a transformer for a sub-class of a given type.
	 * @param <T> Domain type
	 */
	@FunctionalInterface
	interface Factory<T> {
		/**
		 * Creates a transformer for the given type.
		 * @param type Type
		 * @return Transformer
		 */
		NativeTransformer<T> create(Class<? extends T> type);
	}
}
