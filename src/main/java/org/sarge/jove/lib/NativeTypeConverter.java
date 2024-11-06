package org.sarge.jove.lib;

import java.lang.foreign.Arena;

/**
 * A <i>native type converter</i> marshals a Java type to/from its native representation.
 * @param <T> Java type
 * @param <N> Native type
 * @author Sarge
 */
public interface NativeTypeConverter<T, N> extends NativeMapper {
	/**
	 * Marshals the given Java value to its native representation.
	 * @param value		Value
	 * @param type		Java type
	 * @param arena		Arena
	 * @return Native value
	 */
	N toNative(T value, Class<?> type, Arena arena);

	/**
	 * Marshals a native value to its Java equivalent.
	 * @param value		Native value
	 * @param type		Expected Java type
	 * @return Marshalled value
	 */
	T fromNative(N value, Class<?> type);
}
