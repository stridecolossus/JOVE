package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

public class MockStructure implements NativeStructure {
	/**
	 * @return Registry with transformers supporting this mock implementation
	 */
	public static Registry registry() {
		final var registry = new Registry();
		registry.add(int.class, new Primitive(ValueLayout.JAVA_INT));
		return registry;
	}

	public int field;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(JAVA_INT.withName("field"));
	}
}
