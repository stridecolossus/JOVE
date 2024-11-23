package org.sarge.jove.foreign;

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
	 * @param type Target type
	 * @return Native memory layout for the given type
	 */
	MemoryLayout layout(Class<? extends T> type);

	/**
	 * Marshals the given value to its native representation.
	 * @param instance		Instance to marshal
	 * @param context		Native context
	 * @return Native value
	 */
	Object marshal(T instance, NativeContext context);

	/**
	 * Marshals a {@code null} value to its native representation.
	 * @param type Target type
	 * @return Native value
	 */
	Object marshalNull(Class<? extends T> type);

	/**
	 * A <i>return mapper</i> unmarshals method return values and pass-by-reference parameters.
	 * @param <T> Type
	 * @param <R> Native return type
	 */
	interface ReturnMapper<T, R> {
    	/**
    	 * Unmarshals a native return value to a new instance.
    	 * @param value 	Native return value
    	 * @param type		Target type
    	 * @return Marshalled return value
    	 */
		T unmarshal(R value, Class<? extends T> type);
	}

	// TODO
	interface ReturnedParameterMapper<T> {
		/**
		 * Unmarshals a by-reference return value to the given instance.
		 * @param value			Native by-reference return value
		 * @param instance		Instance to populate
		 */
		void unmarshal(MemorySegment value, T instance);
	}
}
