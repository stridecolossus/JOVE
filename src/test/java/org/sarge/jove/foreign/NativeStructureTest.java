package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeStructure.StructureNativeTransformer;
import org.sarge.jove.foreign.NativeTransformer.ParameterMode;

class NativeStructureTest {
	@SuppressWarnings("unused")
	private static class MockStructure implements NativeStructure {
		public int field;
		private int ignored;
		public static final int IGNORED = 0;

		// Required to be instantiated by the framework
		public MockStructure() {
		}

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
			transformer = new StructureNativeTransformer(registry);
		}

		@Test
		void constructor() {
			assertEquals(NativeStructure.class, transformer.type());
		}

		@DisplayName("The layout of a native structure is defined by the structure type")
		@Test
		void layout() {
			assertEquals(structure.layout(), transformer.layout(MockStructure.class));
		}

		@DisplayName("A structure can be transformed to off-heap memory")
		@Test
		void transform() {
			structure.field = 2;
			final MemorySegment address = transformer.transform(structure, ParameterMode.VALUE, Arena.ofAuto());
			assertEquals(2, address.get(JAVA_INT, 0));
		}

		@DisplayName("A null structure can be transformed to off-heap memory")
		@Test
		void empty() {
			assertEquals(MemorySegment.NULL, transformer.empty(MockStructure.class));
		}

		@DisplayName("A structure can be returned from a native method")
		@Test
		void returns() {
			final var structure = new MockStructure();
			final MemorySegment address = arena.allocate(structure.layout());
			address.set(JAVA_INT, 0, 3);

			final var result = (MockStructure) transformer.returns(MockStructure.class).apply(address);
			assertEquals(3, result.field);
		}

		@DisplayName("A structure can be returned from a native method as a by-reference argument")
		@Test
		void update() {
			final var structure = new MockStructure();
			final MemorySegment address = arena.allocate(structure.layout());
			address.set(JAVA_INT, 0, 3);

			transformer.update().accept(address, structure);
			assertEquals(3, structure.field);
		}
	}
}
