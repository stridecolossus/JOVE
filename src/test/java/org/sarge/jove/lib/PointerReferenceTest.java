package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.lib.PointerReference.PointerReferenceNativeMapper;

class PointerReferenceTest {
	private PointerReference ref;

	@BeforeEach
	void before() {
		ref = new PointerReference();
	}

	@Test
	void handle() {
		assertThrows(IllegalStateException.class, () -> ref.handle());
	}

	@Test
	void equals() {
		assertEquals(ref, ref);
		assertNotEquals(ref, null);
		assertNotEquals(ref, new PointerReference());
	}

	@Nested
	class MapperTests {
		private PointerReferenceNativeMapper mapper;

		@BeforeEach
		void before() {
			mapper = new PointerReferenceNativeMapper();
		}

		@Test
		void mapper() {
			assertEquals(PointerReference.class, mapper.type());
			assertEquals(ADDRESS, mapper.layout(null));
		}

		@Test
		void toNative() {
			final MemorySegment address = mapper.marshal(ref, new NativeContext());
			final Handle handle = new Handle(address);
			assertEquals(handle.address(), address);
		}

		@Test
		void toNativeNull() {
			assertThrows(UnsupportedOperationException.class, () -> mapper.marshalNull(null));
		}
	}
}
