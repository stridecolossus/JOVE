package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeReference.*;

class NativeReferenceTest {
	private Factory factory;
	private NativeReferenceTransformer transformer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		factory = new Factory();
		transformer = new NativeReferenceTransformer();
	}

	@Test
	void unmarshal() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal());
	}

	@Test
	void equals() {
		final var ref = factory.integer();
		transformer.marshal(ref, allocator);
		assertEquals(ref, ref);
		assertNotEquals(ref, factory.integer());
		assertNotEquals(ref, null);
	}

	@Nested
	class IntegerReferenceTest {
		private NativeReference<Integer> integer;

		@BeforeEach
		void before() {
			integer = factory.integer();
		}

		@Test
		void empty() {
			assertEquals(0, integer.get());
		}

		@Test
		void marshal() {
			final MemorySegment address = transformer.marshal(integer, allocator);
			address.set(ValueLayout.JAVA_INT, 0, 42);
			assertEquals(42, integer.get());
		}
	}

	@Nested
	class PointerReferenceTest {
		private NativeReference<Handle> pointer;

		@BeforeEach
		void before() {
			pointer = factory.pointer();
		}

		@Test
		void empty() {
			assertEquals(null, pointer.get());
		}

		@Test
		void marshal() {
			final MemorySegment address = transformer.marshal(pointer, allocator);
			final MemorySegment handle = MemorySegment.ofAddress(42);
			address.set(ValueLayout.ADDRESS, 0, handle);
			assertEquals(new Handle(handle), pointer.get());
		}
	}
}
