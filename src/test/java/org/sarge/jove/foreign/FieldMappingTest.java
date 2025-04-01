package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.List;

import org.junit.jupiter.api.*;

class FieldMappingTest {
	private List<FieldMapping> mappings;
	private MockStructure structure;
	private Registry registry;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		structure = new MockStructure();
		registry = new Registry();
		registry.add(int.class, new Primitive(JAVA_INT));
		allocator = Arena.ofAuto();
		mappings = FieldMapping.build(MockStructure.class, structure.layout(), registry);
	}

	@Test
	void build() {
		assertEquals(1, mappings.size());
	}

	@Test
	void marshal() {
		final MemorySegment address = allocator.allocate(structure.layout());
		structure.field = 42;
		mappings.getFirst().marshal(structure, address, allocator);
		assertEquals(42, address.get(JAVA_INT, 0L));
	}

	@Test
	void unmarshal() {
		final MemorySegment address = allocator.allocate(structure.layout());
		address.set(JAVA_INT, 0L, 42);
		mappings.getFirst().unmarshal(address, structure);
		assertEquals(42, structure.field);
	}

	@Test
	void anonymous() {
		final StructLayout layout = MemoryLayout.structLayout(JAVA_INT);
		assertThrows(IllegalArgumentException.class, () -> FieldMapping.build(MockStructure.class, layout, registry));
	}

	@Test
	void access() throws Exception {
		class Invalid implements NativeStructure {
			@SuppressWarnings("unused")
			private int field;

			@Override
			public StructLayout layout() {
				return MemoryLayout.structLayout(JAVA_INT.withName("field"));
			}
		}

		assertThrows(RuntimeException.class, () -> FieldMapping.build(Invalid.class, structure.layout(), registry));
	}
}
