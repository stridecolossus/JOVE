package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;
import java.lang.reflect.Field;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.FieldMapping.*;

class NestedStructureFieldMappingTest {
	/**
	 * Define a structure that can be nested.
	 */
	private static class NestedStructure implements NativeStructure {
		public int integer;

		@Override
		public StructLayout layout() {
			return MemoryLayout.structLayout(
					JAVA_INT.withName("integer")
			);
		}
	}

	/**
	 * Define a structure with a nested structure instance
	 * Note that the layout is cloned from the nested structure.
	 */
	private static class ParentStructure implements NativeStructure {
		public NestedStructure nested = new NestedStructure();			// TODO - auto instantiate???

		@Override
		public StructLayout layout() {
			return MemoryLayout.structLayout(
					MemoryLayout.structLayout(JAVA_INT.withName("integer")).withName("nested")
			);
		}
	}

	private ParentStructure parent;
	private NestedStructureFieldMapping mapping;
	private MemorySegment address;
	private Arena arena;
	private TransformerRegistry registry;

	@BeforeEach
	void before() throws Exception {
		// Init supported transformers
		registry = new TransformerRegistry();
		registry.add(new PrimitiveNativeTransformer<>(int.class));

		// Allocate off-heap structure
		arena = Arena.ofAuto();
		parent = new ParentStructure();
		address = arena.allocate(parent.layout());

		// Create nested field mapping
		final Field field = ParentStructure.class.getDeclaredField("nested");
		final var delegate = StructureFieldMapping.build(NestedStructure.class, parent.nested.layout(), registry);
		mapping = new NestedStructureFieldMapping(field, delegate);
	}

	@DisplayName("A structure containing a nested structure can be transformed to off-heap memory")
	@Test
	void transform() {
		parent.nested.integer = 2;
		mapping.transform(parent, address, arena);
		assertEquals(2, address.get(JAVA_INT, 0L));
	}

	@DisplayName("A structure containing a nested structure can be returned from a native method as a by-reference parameter")
	@Test
	void populate() {
		address.set(JAVA_INT, 0L, 3);
		mapping.populate(address, parent);
		assertEquals(3, parent.nested.integer);
	}
}
