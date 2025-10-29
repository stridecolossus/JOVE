package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;

class PointerTest {
	private Pointer pointer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		pointer = new Pointer();
	}

	@Test
	void empty() {
		assertEquals(null, pointer.get());
	}

	@Test
	void update() {
		final Handle handle = new Handle(3);
		final MemorySegment address = allocator.allocate(AddressLayout.ADDRESS);
		address.set(ValueLayout.ADDRESS, 0L, handle.address());
		pointer.update(address);
		assertEquals(handle, pointer.get());
	}

	@Test
	void none() {
		final MemorySegment address = allocator.allocate(AddressLayout.ADDRESS);
		pointer.update(address);
		assertEquals(null, pointer.get());
	}
}
