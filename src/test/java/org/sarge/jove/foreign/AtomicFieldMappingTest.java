package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.FieldMapping.AtomicFieldMapping;

class AtomicFieldMappingTest {
	private static class MockStructure implements NativeStructure {
		public int integer;
		public String string;

		@Override
		public StructLayout layout() {
			return MemoryLayout.structLayout(
					JAVA_INT.withName("integer"),
					PADDING,
					POINTER.withName("string")
			);
		}
	}

	private MockStructure structure;
	private MemorySegment address;
	private Arena arena;
	private TransformerRegistry registry;

	@BeforeEach
	void before() {
		structure = new MockStructure();
		registry = new TransformerRegistry();
		arena = Arena.ofAuto();
		address = arena.allocate(structure.layout());
	}

	@Nested
	class PrimitiveTests {
		private PrimitiveNativeTransformer<?> transformer;
		private AtomicFieldMapping mapping;

		@BeforeEach
		void before() throws Exception {
			final Field field = MockStructure.class.getDeclaredField("integer");
			final VarHandle handle = structure.layout().varHandle(PathElement.groupElement("integer"));
			transformer = new PrimitiveNativeTransformer<>(int.class);
			registry.add(transformer);
			mapping = new AtomicFieldMapping(field, transformer, handle);
		}

		@Test
		void transform() throws Exception {
			structure.integer = 2;
			mapping.transform(structure, address, arena);
			assertEquals(2, address.get(JAVA_INT, 0L));
		}

		@Test
		void populate() throws Exception {
			address.set(JAVA_INT, 0L, 3);
			mapping.populate(address, structure);
			assertEquals(3, structure.integer);
		}
	}

	@Nested
	class PointerTests {
		private StringNativeTransformer transformer;
		private AtomicFieldMapping mapping;

		@BeforeEach
		void before() throws Exception {
			final Field field = MockStructure.class.getDeclaredField("string");
			final VarHandle handle = structure.layout().varHandle(PathElement.groupElement("string"));
			transformer = new StringNativeTransformer();
			registry.add(transformer);
			mapping = new AtomicFieldMapping(field, transformer, handle);
		}

		@Test
		void transform() throws Exception {
			structure.string = "string";
			mapping.transform(structure, address, arena);
			assertEquals("string", address.get(ADDRESS, 8L).reinterpret(Integer.MAX_VALUE).getString(0L));
		}

		@Test
		void populate() throws Exception {
			address.set(ADDRESS, 8L, arena.allocateFrom("string"));
			mapping.populate(address, structure);
			assertEquals("string", structure.string);
		}
	}
}
