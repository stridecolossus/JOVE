package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

public class MockStructure implements NativeStructure {
	public int field;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("field"),
				MemoryLayout.paddingLayout(4)
		);
	}
}
