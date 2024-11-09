package org.sarge.jove.lib;

import java.lang.foreign.*;

/**
 * A <i>native mapper</i> defines a domain type that can be marshalled to its corresponding native representation.
 * @param <T> Type
 * @author Sarge
 */
public interface NativeMapper<T> {
	/**
	 * @return Type
	 */
	Class<T> type();

	/**
	 * @return Native type
	 */
	MemoryLayout layout();

	/**
	 * Marshals the given value to its native representation.
	 * @param value		Value to marshal
	 * @param arena		Arena
	 * @return Native value
	 */
	Object toNative(T value, Arena arena);

	/**
	 * Marshals a {@code null} value.
	 * @param type Target type
	 * @return Native {@code null} value
	 * @throws UnsupportedOperationException if this type cannot be {@code null}
	 */
	Object toNativeNull(Class<?> type);

	/**
	 * A <i>return converter</i> denotes a type that can also be returned from a native method.
	 * @param <R> Native return type
	 */
	interface ReturnMapper<R> {
    	/**
    	 * Unmarshals a native return value.
    	 * @param value		Native return value to unmarshal
    	 * @param type		Target type
    	 * @return Return value
    	 */
		Object fromNative(R value, Class<?> type);
	}
}
