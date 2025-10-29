package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.Handle.HandleTransformer;

class ArrayTransformerTest {
	private Transformer component;
	private ArrayTransformer transformer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		component = new HandleTransformer();
		transformer = new ArrayTransformer(component);
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.ADDRESS, transformer.layout());
	}

	@DisplayName("An array of a supported type can be marshalled")
	@Test
	void marshal() {
		final Handle[] array = {new Handle(3)};
		final MemorySegment address = transformer.marshal(array, allocator);
		assertEquals(MemorySegment.ofAddress(3), address.reinterpret(8).getAtIndex(ValueLayout.ADDRESS, 0));
	}

	@DisplayName("Empty array elements can be marshalled")
	@Test
	void empty() {
		final Handle[] array = {null};
		final MemorySegment address = transformer.marshal(array, allocator);
		assertEquals(MemorySegment.NULL, address.reinterpret(8).getAtIndex(ValueLayout.ADDRESS, 0));
	}

	@DisplayName("An array cannot be returned from a native method")
	@Test
	void unmarshal() {
//		assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal());
	}
}
