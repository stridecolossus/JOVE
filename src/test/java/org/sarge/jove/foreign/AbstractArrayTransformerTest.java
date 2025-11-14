package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;
import java.util.List;

import org.junit.jupiter.api.*;

class AbstractArrayTransformerTest {
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
	}

	@Test
	void unmarshal() {
		final String string = "string";
		final MemorySegment address = allocator.allocate(ValueLayout.ADDRESS, 2);
		address.setAtIndex(ValueLayout.ADDRESS, 0L, allocator.allocateFrom(string));
		address.setAtIndex(ValueLayout.ADDRESS, 1L, allocator.allocateFrom(string));
		assertEquals(List.of(string, string), AbstractArrayTransformer.unmarshal(address, 2, StringTransformer::unmarshal));
	}
}
