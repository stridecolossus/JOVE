package org.sarge.jove.lib;

import java.lang.foreign.ValueLayout;

/**
 * A <i>native mapper</i> maps a Java type to its native representation.
 * @author Sarge
 */
public interface NativeMapper {
	/**
	 * @return Java type
	 */
	Class<?> type();

	/**
	 * @return Native type
	 */
	ValueLayout layout();
}
