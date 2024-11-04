package org.sarge.jove.lib;

import java.lang.foreign.*;

public class IntegerNativeMapper implements NativeMapper<Integer> {
	@Override
	public Class<Integer> type() {
		return int.class;
	}

	@Override
	public ValueLayout layout() {
		return ValueLayout.JAVA_INT;
	}

	@Override
	public Object toNative(Object value, Arena arena) {
		return value;
	}

	@Override
	public Object fromNative(Object value) {
		return value;
	}
}
