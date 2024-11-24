package org.sarge.jove.foreign;

import java.lang.foreign.MemoryLayout;
import java.util.function.*;

/**
 * A <i>native mapper</i> defines a domain type that can be marshalled to/from a corresponding native representation.
 * @param <T> Type
 * @author Sarge
 */
public interface NativeMapper<T, R> {
	/**
	 * @return Type
	 */
	Class<T> type();

	/**
	 * @param type Target type
	 * @return Native memory layout for the given type
	 */
	MemoryLayout layout(Class<? extends T> target);

	/**
	 * Marshals the given value to its native representation.
	 * @param instance		Instance to marshal
	 * @param context		Native context
	 * @return Native value
	 */
	Object marshal(T instance, NativeContext context);

	/**
	 * Marshals a {@code null} value to its native representation.
	 * @param target Target type
	 * @return Native value
	 */
	Object marshalNull(Class<? extends T> target);

	/**
	 * Provides a mapper for a return value of this native type.
	 * @param target Target type
	 * @return Return value mapper
	 */
	Function<R, T> returns(Class<? extends T> target);

	/**
	 * Provides a mapper for a by-reference parameter for this native type.
	 * @param target Target type
	 * @return Returned parameter mapper
	 * @see Returned
	 */
	BiConsumer<R, T> unmarshal(Class<? extends T> target);
}
