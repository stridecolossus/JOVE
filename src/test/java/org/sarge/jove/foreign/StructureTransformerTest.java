package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeStructure.StructureTransformer;

class StructureTransformerTest {
	private StructureTransformer transformer;
	private Registry registry;
	private Arena arena;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
		registry = Registry.create();
		transformer = StructureTransformer.create(MockStructure.class, registry);
	}

	@Test
	void layout() {
		final var structure = new MockStructure();
		assertEquals(structure.layout(), transformer.layout());
	}

	@Test
	void marshal() {
		final var structure = new MockStructure();
		structure.field = 42;

		final MemorySegment address = transformer.marshal(structure, arena);
		assertEquals(4, address.byteSize());
		assertEquals(42, address.get(JAVA_INT, 0L));
	}

	@Test
	void unmarshal() {
		final var instance = new MockStructure();
		final MemorySegment address = arena.allocate(instance.layout());
		address.set(JAVA_INT, 0, 42);

		final MockStructure structure = (MockStructure) transformer.unmarshal().apply(address);
		assertEquals(42, structure.field);
	}
}

// TODO - FieldMappingTest
//
//	@SuppressWarnings("unused")
//	private static class Anonymous implements NativeStructure {
//		public Anonymous() {
//		}
//
//		@Override
//		public StructLayout layout() {
//			return MemoryLayout.structLayout(JAVA_INT);
//		}
//	}
//
//	@Test
//	void named() {
//		assertThrows(IllegalArgumentException.class, () -> StructureTransformer.create(Anonymous.class, registry));
//	}
//
//	@SuppressWarnings("unused")
//	private static class Invalid implements NativeStructure {
//		private int field;
//
//		public Invalid() {
//		}
//
//		@Override
//		public StructLayout layout() {
//			return MemoryLayout.structLayout(JAVA_INT.withName("field"));
//		}
//	}
//
//	@Test
//	void access() {
//		assertThrows(RuntimeException.class, () -> StructureTransformer.create(Invalid.class, mapper));
//	}
//}
