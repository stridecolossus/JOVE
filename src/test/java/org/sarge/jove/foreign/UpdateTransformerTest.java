package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class UpdateTransformerTest {
	private UpdateTransformer<MockStructure> transformer;

	@BeforeEach
	void before() {
		transformer = new UpdateTransformer<>(new MockStructureTransformer());
	}

	@Test
	void layout() {
		assertEquals(AddressLayout.ADDRESS, transformer.layout());
	}

	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, transformer.empty());
	}

	@Test
	void update() {
		final MemorySegment address = MemorySegment.ofAddress(42);
		final var structure = new MockStructure();
		transformer.update().accept(address, structure);
		assertEquals(42, structure.field);
	}
}
