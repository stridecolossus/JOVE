package org.sarge.jove.lib;

import java.lang.foreign.*;

/**
 * A <i>native mapper</i> defines a domain type that can be marshalled to/from a corresponding native representation.
 * @param <T> Type
 * @author Sarge
 */
public interface NativeMapper<T> {
	/**
	 * @return Type
	 */
	Class<T> type();

	/**
	 * @return Native type layout
	 */
	MemoryLayout layout();

	/**
	 * Marshals the given value to its native representation.
	 * @param value 		Value to marshal
	 * @param context		Native context
	 * @return Native value
	 */
	Object toNative(T value, NativeContext context);

	/**
	 * Marshals a {@code null} value to its native representation.
	 * @param type Target type
	 * @return Native value
	 */
	default Object toNativeNull(Class<? extends T> type) {
		return MemorySegment.NULL;
	}

	/**
	 * A <i>return converter</i> denotes a type that can also be returned from a native method.
	 * @param <T> Type
	 * @param <R> Native return type
	 */
	interface ReturnMapper<T, R> extends NativeMapper<T> {
    	/**
    	 * Unmarshals a native return value.
    	 * @param value 	Native return value
    	 * @param type		Target type
    	 * @return Marshalled return value
    	 */
		Object fromNative(R value, Class<? extends T> type);
	}
}
