package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class StringNativeTransformerTest {
	private StringNativeTransformer transformer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		transformer = new StringNativeTransformer();
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.ADDRESS, transformer.layout());
	}

	@Test
	void marshal() {
		final String string = "whatever";
		final MemorySegment address = (MemorySegment) transformer.marshal(string, allocator);
		assertEquals(1 + string.length(), address.byteSize());
	}

	@Test
	void unmarshal() {
		final String string = "whatever";
		final MemorySegment memory = allocator.allocateFrom(string);
		assertEquals(string, transformer.unmarshal().apply(memory));
	}
}
