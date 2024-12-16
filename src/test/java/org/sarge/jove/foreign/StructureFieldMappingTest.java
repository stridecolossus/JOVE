package org.sarge.jove.foreign;

import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.FieldMapping.StructureFieldMapping;

class StructureFieldMappingTest {
	private static class MockStructure implements NativeStructure {
		public int field;

		@Override
		public StructLayout layout() {
			return structLayout(JAVA_INT.withName("field"));
		}
	}

	private MockStructure structure;
	private StructureFieldMapping mapping;
	private TransformerRegistry registry;
	private MemorySegment address;
	private Arena arena;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
		address = arena.allocate(AddressLayout.ADDRESS);
		registry = new TransformerRegistry();
		registry.add(new PrimitiveNativeTransformer<>(int.class));
		structure = new MockStructure();
		mapping = StructureFieldMapping.build(MockStructure.class, structure.layout(), registry);
	}

	@DisplayName("A field mapping should be generated for each public member of the structure")
	@Test
	void mappings() {
		assertEquals(1, mapping.mappings().size());
	}

	@DisplayName("A structure can be marshalled to off-heap memory")
	@Test
	void transform() {
		structure.field = 2;
		mapping.transform(structure, address, arena);
		assertEquals(2, address.get(JAVA_INT, 0L));
	}

	@DisplayName("A structure can be marshalled from off-heap memory")
	@Test
	void populate() {
		address.set(JAVA_INT, 0L, 3);
		mapping.populate(address, structure);
		assertEquals(3, structure.field);
	}

	@DisplayName("The layout of a native structure must match the structure fields by name")
	@Test
	void unknown() {
		final var unknown = new NativeStructure() {
			@SuppressWarnings("unused")
			public int field;

			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(JAVA_INT.withName("cobblers"));
			}
		};
		assertThrows(IllegalArgumentException.class, () -> StructureFieldMapping.build(unknown.getClass(), unknown.layout(), registry));
	}

	@DisplayName("The member layout of a native structure field must be a supported type")
	@Test
	void invalid() {
		final var invalid = new NativeStructure() {
			@SuppressWarnings("unused")
			public int field;

			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(MemoryLayout.paddingLayout(4).withName("field"));
			}
		};
		assertThrows(IllegalArgumentException.class, () -> StructureFieldMapping.build(invalid.getClass(), invalid.layout(), registry));
	}
}
