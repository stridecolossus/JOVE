package org.sarge.jove.lib;

import java.lang.foreign.*;

/**
 * A <i>default native mapper</i> defines a type that has a native layout but is marshalled as-is, i.e. primitive or wrapper types that are automagically handled by FFM.
 * @param <T> Type
 * @author Sarge
 */
public class DefaultNativeMapper<T> extends AbstractNativeMapper<T> {
	/**
	 * Constructor.
	 * @param type		Type
	 * @param layout	Native layout
	 */
	public DefaultNativeMapper(Class<T> type, MemoryLayout layout) {
		super(type, layout);
	}

	@Override
	public Object toNative(T value, Arena arena) {
		return value;
	}
}
