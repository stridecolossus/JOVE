package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class StructureTransformerTest {
	private MockStructure structure;
	private StructureTransformer transformer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		final var factory = new StructureTransformerFactory(DefaultRegistry.create());
		transformer = factory.transformer(MockStructure.class);
		allocator = Arena.ofAuto();
		structure = new MockStructure();
	}

	@Test
	void layout() {
		assertEquals(structure.layout(), transformer.layout());
	}

	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, transformer.empty());
	}

	@DisplayName("A native structure can be marshalled to off-heap memory")
	@Test
	void marshal() {
		// Populate structure
		structure.field = 3;

		// Marshal structure and check off-heap memory
		final MemorySegment address = transformer.marshal(structure, allocator);
		assertEquals(4, address.byteSize());
		assertEquals(3, address.get(JAVA_INT, 0L));
	}

	@DisplayName("A native structure can be marshalled from off-heap memory")
	@Test
	void unmarshal() {
		// Init off-heap representation
		final MemorySegment address = allocator.allocate(structure.layout());
		address.set(JAVA_INT, 0L, 3);

		// Unmarshal structure instance from the off-heap memory
		final var result = (MockStructure) transformer.unmarshal().apply(address);
		assertEquals(structure.layout(), result.layout());
		assertEquals(3, result.field);
	}

	@Test
	void update() {
		final MemorySegment address = allocator.allocate(structure.layout());
		address.set(JAVA_INT, 0L, 3);
		transformer.update().accept(address, structure);
		assertEquals(3, structure.field);
	}
}
