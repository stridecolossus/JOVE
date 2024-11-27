package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeStructure.StructureNativeTransformer;

class StructureNativeTransformerTest {
	protected static class MockStructure extends NativeStructure {
		public int field;

		@Override
		public StructLayout layout() {
			return MemoryLayout.structLayout(JAVA_INT.withName("field"));
		}
	}

	private StructureNativeTransformer transformer;
	private MockStructure structure;

	@BeforeEach
	void before() {
		final var registry = new TransformerRegistry();
		registry.add(new PrimitiveNativeTransformer<>(int.class));
		transformer = new StructureNativeTransformer().derive(MockStructure.class, registry);
		structure = new MockStructure();
	}

	@Test
	void constructor() {
		assertEquals(NativeStructure.class, transformer.type());
		assertEquals(structure.layout(), transformer.layout());
	}

	@Test
	void transform() {
		structure.field = 2;
		final MemorySegment address = transformer.transform(structure, Arena.ofAuto());
		assertEquals(2, address.get(JAVA_INT, 0));
	}

	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, transformer.empty());
	}

	private static MemorySegment address() {
		final var structure = new MockStructure();
		final MemorySegment address = Arena.ofAuto().allocate(structure.layout());
		address.set(JAVA_INT, 0, 3);
		return address;
	}

	@Test
	void returns() {
		final MemorySegment address = address();
		final var result = (MockStructure) transformer.returns().apply(address);
		assertEquals(3, result.field);
	}

	@Test
	void update() {
		final var structure = new MockStructure();
		final MemorySegment address = address();
		transformer.update().accept(address, structure);
		assertEquals(3, structure.field);
	}
}
