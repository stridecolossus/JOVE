package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeReference.*;

class NativeReferenceTest {
	private NativeReferenceTransformer transformer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		transformer = new NativeReferenceTransformer();
	}

	@DisplayName("A native reference can be explicitly overridden with a literal value")
	@Test
	void set() {
		final var ref = new NativeReference<>() {
			@Override
			protected Object update(MemorySegment pointer) {
				return null;
			}
		};
		final Object obj = new Object();
		ref.set(obj);
		transformer.marshal(ref, allocator);
		assertEquals(obj, ref.get());
	}

	@Test
	void integer() {
		final var integer = new IntegerReference();
		final MemorySegment address = transformer.marshal(integer, allocator);
		address.set(ValueLayout.JAVA_INT, 0, 3);
		assertEquals(3, integer.get());
	}

	@Test
	void pointer() {
		final var pointer = new Pointer();
		final MemorySegment address = transformer.marshal(pointer, allocator);
		final Handle handle = new Handle(3);
		address.set(ValueLayout.ADDRESS, 0, handle.address());
		assertEquals(handle, pointer.get());
	}
}
