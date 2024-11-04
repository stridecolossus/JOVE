package org.sarge.jove.lib;

import java.lang.foreign.*;

public class StringNativeMapper implements NativeMapper<String> {
	@Override
	public Class<String> type() {
		return String.class;
	}

	@Override
	public ValueLayout layout() {
		return ValueLayout.ADDRESS;
	}

	@Override
	public Object toNative(Object value, Arena arena) {

		arena.allocate

		return null;
	}

	@Override
	public String fromNative(Object value) {

		final var segment = (MemorySegment) value;
		final String str = segment.reinterpret(Integer.MAX_VALUE).getString(0);

		return null;
	}
}
