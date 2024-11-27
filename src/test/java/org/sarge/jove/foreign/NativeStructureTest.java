package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class NativeStructureTest {
	@SuppressWarnings("unused")
	private static class MockStructure extends NativeStructure {
		public int field;
		private int ignored;
		public static final int IGNORED = 0;

		@Override
		public StructLayout layout() {
			return MemoryLayout.structLayout(
					JAVA_INT.withName("field"),
					MemoryLayout.paddingLayout(4)
			);
		}
	}

	private MockStructure structure;
	private NativeMapperRegistry registry;

	@BeforeEach
	void before() {
		structure = new MockStructure();
		registry = new NativeMapperRegistry();
	}

	@Test
	void equals() {
		assertEquals(structure, structure);
		assertEquals(structure, new MockStructure());
		assertNotEquals(structure, null);
		assertNotEquals(structure, mock(NativeStructure.class));
	}

	private void register() {
		final NativeMapper mapper = new PrimitiveNativeMapper<>(int.class);
		registry.add(mapper);
	}

	@DisplayName("The field mappings for a native structure can be constructed from its layout")
	@Test
	void build() throws Exception {

		register();

		// TODO - test structure::un/marshal and/or underlying field mappings? structure mapper? both?

//		// Build expected field mapping
		final StructLayout layout = structure.layout();
//		final VarHandle handle = layout.varHandle(PathElement.groupElement("field"));
//		final var expected = new FieldMapping(field, handle, mapper);

		// Build field mappings for this structure
		final StructureFieldMapping fields = StructureFieldMapping.build(layout, 0, MockStructure.class, registry);

//		fields.getFirst().marshal(structure, address, null);
//		assertEquals(List.of(expected), fields);
	}

	@DisplayName("A native structure cannot declare an unsupported native type")
	@Test
	void unsupported() {
		assertThrows(IllegalArgumentException.class, () -> StructureFieldMapping.build(structure.layout(), 0, MockStructure.class, registry));
	}

	@DisplayName("A native structure cannot define a layout that does not match its fields")
	@Test
	void unknown() {
		final var layout = MemoryLayout.structLayout(JAVA_INT.withName("cobblers"));
		register();
		assertThrows(IllegalArgumentException.class, () -> StructureFieldMapping.build(layout, 0, MockStructure.class, registry));
	}

	@DisplayName("A native structure cannot contain a public field that is not declared in its layout")
	@Test
	void undeclared() {
		@SuppressWarnings("unused")
		final var invalid = new NativeStructure() {
			public int doh;

			@Override
			public StructLayout layout() {
				return structure.layout();
			}
		};
		register();
		assertThrows(IllegalArgumentException.class, () -> StructureFieldMapping.build(invalid.layout(), 0, invalid.getClass(), registry));
	}
}
