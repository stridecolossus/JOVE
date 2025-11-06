package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class IntegerReferenceTest {
	private IntegerReference integer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		integer = new IntegerReference();
	}

	@Test
	void zero() {
		assertEquals(0, integer.get());
	}

	@Test
	void update() {
		final MemorySegment address = allocator.allocate(AddressLayout.ADDRESS);
		address.set(ValueLayout.JAVA_INT, 0L, 3);
		assertEquals(3, integer.update(address));
	}
}
