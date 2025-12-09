package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;

class IntegerReferenceTest {
	private IntegerReference integer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		integer = new IntegerReference();
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.ADDRESS, integer.layout());
	}

	@Test
	void zero() {
		assertEquals(0, integer.get());
	}

	@Test
	void set() {
		integer.set(2);
		assertEquals(2, integer.get());
	}

	@Test
	void update() {
		final var transformer = new NativeReferenceTransformer();
		final MemorySegment address = transformer.marshal(integer, allocator);
		address.set(ValueLayout.JAVA_INT, 0L, 3);
		assertEquals(3, integer.get());
	}
}
