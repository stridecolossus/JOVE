package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

public class MockStructure implements NativeStructure {
	public static final StructLayout LAYOUT = MemoryLayout.structLayout(JAVA_INT.withName("field"));

	public int field;

	@Override
	public StructLayout layout() {
		return LAYOUT;
	}
}
