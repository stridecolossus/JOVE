package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeStructure.StructureNativeTransformer;

class NativeStructureTest {
	@SuppressWarnings("unused")
	static class MockStructure implements NativeStructure {
		public int field;
		private int ignored;
		public static final int IGNORED = 0;

		@Override
		public StructLayout layout() {
			return MemoryLayout.structLayout(JAVA_INT.withName("field"));
		}
	}

	private MockStructure structure;

	@BeforeEach
	void before() {
		structure = new MockStructure();
	}

//	@Test
//	void equals() {
//		assertEquals(structure, structure);
//		assertEquals(structure, new MockStructure());
//		assertNotEquals(structure, null);
//		assertNotEquals(structure, mock(NativeStructure.class));
//	}

	@Nested
	class TransformerTests {
		private NativeTransformer<NativeStructure, MemorySegment> transformer;
		private TransformerRegistry registry;
		private Arena arena;

		@BeforeEach
		void before() {
			arena = Arena.ofAuto();
			registry = new TransformerRegistry();
			registry.add(new PrimitiveNativeTransformer<>(int.class));
			transformer = new StructureNativeTransformer(registry).derive(MockStructure.class);
		}

		@Test
		void constructor() {
			assertEquals(MockStructure.class, transformer.type());
			assertEquals(structure.layout(), transformer.layout());
		}

		@Test
		void transform() {
			structure.field = 2;
			final MemorySegment address = (MemorySegment) transformer.transform(structure, arena);
			assertEquals(2, address.get(JAVA_INT, 0));
		}

		@Test
		void empty() {
			assertEquals(MemorySegment.NULL, transformer.empty());
		}

		private MemorySegment address() {
			final var structure = new MockStructure();
			final MemorySegment address = arena.allocate(structure.layout());
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

		@Test
		void unsupported() {
			final var registry = new TransformerRegistry();
			assertThrows(IllegalArgumentException.class, () -> new StructureNativeTransformer(registry).derive(MockStructure.class));
		}

		static class Invalid implements NativeStructure {
			public int field;

			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(JAVA_INT.withName("cobblers"));
			}
		}

		@Test
		void unknown() {
			assertThrows(IllegalArgumentException.class, () -> new StructureNativeTransformer(registry).derive(Invalid.class));
		}
	}
}
