package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class StringTransformerTest {
	private StringTransformer transformer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		transformer = new StringTransformer();
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.ADDRESS, transformer.layout());
	}

	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, transformer.empty());
	}

	@Test
	void marshal() {
		final String string = "whatever";
		final MemorySegment address = transformer.marshal(string, allocator);
		assertEquals(string, address.getString(0));
	}

	@Test
	void unmarshal() {
		final String string = "whatever";
		final MemorySegment address = allocator.allocateFrom(string);
		assertEquals(string, transformer.unmarshal(address));
	}

	@Test
	void update() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.update());
	}
}
