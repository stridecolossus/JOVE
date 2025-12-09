package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;

class NativeReferenceTest {
	private NativeReference<MemorySegment> reference;
	private SegmentAllocator allocator;
	private NativeReferenceTransformer transformer;

	@BeforeEach
	void before() {
		reference = new NativeReference<>(ADDRESS) {
			@Override
			protected MemorySegment unmarshal(MemorySegment address, AddressLayout layout) {
				return address.get(ADDRESS, 0L);
			}
		};
		transformer = new NativeReferenceTransformer();
		allocator = Arena.ofAuto();
	}

	@Test
	void empty() {
		assertEquals(null, reference.get());
	}

	@Test
	void set() {
		final var address = MemorySegment.ofAddress(42);
		reference.set(address);
		assertEquals(address, reference.get());
	}

	@Test
	void update() {
		final MemorySegment pointer = transformer.marshal(reference, allocator);
		final var address = MemorySegment.ofAddress(42);
		pointer.set(ADDRESS, 0L, address);
		assertEquals(address, reference.get());
	}

	@Test
	void equals() {
		assertEquals(reference, reference);
		assertNotEquals(reference, null);
	}
}
