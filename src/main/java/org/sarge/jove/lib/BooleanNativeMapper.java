package org.sarge.jove.lib;

import java.lang.foreign.*;

public class BooleanNativeMapper implements NativeMapper<Boolean> {
	@Override
	public Class<Boolean> type() {
		return boolean.class;
	}

	@Override
	public ValueLayout layout() {
		return ValueLayout.JAVA_INT;
	}

	@Override
	public Object toNative(Object value, Arena arena) {
		return Boolean.TRUE.equals(value) ? 1 : 0;
	}

	@Override
	public Boolean fromNative(Object value) {
		return value.equals(1) ? Boolean.TRUE : Boolean.FALSE;
	}
}
