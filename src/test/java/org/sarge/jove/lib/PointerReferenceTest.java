package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.lib.PointerReference.PointerReferenceNativeMapper;

class PointerReferenceTest {
	private PointerReference ref;
	private Arena arena;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
		ref = new PointerReference(arena);
	}

	@Test
	void handle() {
		final Handle handle = ref.handle();
		assertNotNull(handle.address());
	}

	@Test
	void equals() {
		assertEquals(ref, ref);
		assertNotEquals(ref, null);
		assertNotEquals(ref, new PointerReference(arena));
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
			assertEquals(ADDRESS, mapper.layout());
		}

		@Test
		void toNative() {
			final MemorySegment address = mapper.toNative(ref, arena);
			assertEquals(ref.handle().address(), address);
		}

		@Test
		void toNativeNull() {
			assertThrows(UnsupportedOperationException.class, () -> mapper.toNativeNull(PointerReference.class));
		}
	}
}
