package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.Handle.HandleTransformer;

class ArrayTransformerTest {
	private Transformer<Handle> component;
	private ArrayTransformer<Handle> transformer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		component = new HandleTransformer();
		transformer = new ArrayTransformer<>(component);
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.ADDRESS, transformer.layout());
	}

	@Test
	void marshal() {
		final Handle[] array = {new Handle(42)};
		final MemorySegment address = transformer.marshal(array, allocator);
		assertEquals(MemorySegment.ofAddress(42), address.reinterpret(8).getAtIndex(ValueLayout.ADDRESS, 0));
	}

	@Test
	void empty() {
		final Handle[] array = {null};
		final MemorySegment address = transformer.marshal(array, allocator);
		assertEquals(MemorySegment.NULL, address.reinterpret(8).getAtIndex(ValueLayout.ADDRESS, 0));
	}

	@Test
	void unmarshal() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal());
	}
}
