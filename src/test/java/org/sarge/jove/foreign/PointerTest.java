package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;

class PointerTest {
	private Pointer pointer;
	private NativeReferenceTransformer transformer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		transformer = new NativeReferenceTransformer();
		pointer = new Pointer();
	}

	@Test
	void layout() {
		assertEquals(ADDRESS, pointer.layout());
	}

	@Test
	void empty() {
		assertEquals(null, pointer.get());
	}

	@Test
	void set() {
		final var handle = new Handle(2);
		pointer.set(handle);
		assertEquals(handle, pointer.get());
	}

	@Test
	void update() {
		final MemorySegment address = transformer.marshal(pointer, allocator);
		final Handle handle = new Handle(3);
		address.set(ADDRESS, 0L, handle.address());
		assertEquals(handle, pointer.get());
	}

	@Test
	void none() {
		transformer.marshal(pointer, allocator);
		assertEquals(null, pointer.get());
	}

	@Test
	void block() {
		// Create pointer with a specifically sized memory block
		final AddressLayout expected = ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(4, JAVA_BYTE));
		pointer = new Pointer(4);
		assertEquals(expected, pointer.layout());

		// Mock off-heap block
		final MemorySegment data = allocator.allocate(pointer.layout());
		data.setAtIndex(JAVA_BYTE, 0L, (byte) 42);

		// Update pointer
		final MemorySegment address = transformer.marshal(pointer, allocator);
		address.set(pointer.layout(), 0L, data);

		// Check result is the expected size and value
		final MemorySegment result = pointer.get().address();
		assertEquals(4, result.byteSize());
		assertEquals((byte) 42, result.getAtIndex(JAVA_BYTE, 0L));
	}
}
