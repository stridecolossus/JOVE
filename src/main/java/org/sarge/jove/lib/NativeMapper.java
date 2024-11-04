package org.sarge.jove.lib;

import java.lang.foreign.*;

/**
 * A <i>native mapper</i> marshals a Java type to/from the native representation.
 * @author Sarge
 */
public interface NativeMapper<T> {
	/**
	 * @return Java type
	 */
	Class<T> type();

	/**
	 * @return Native type
	 */
	ValueLayout layout();

	/**
	 * Marshals the given Java type to its native representation.
	 * @param value Java value
	 * @param arena Arena
	 * @return Native value
	 */
	Object toNative(Object value, Arena arena);

	/**
	 * Marshals the given native value to the Java equivalent.
	 * @param value Native value
	 * @return Java value
	 */
	Object fromNative(Object value);
}
