package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.lib.IntegerReference.IntegerReferenceNativeMapper;

class IntegerReferenceTest {
	private IntegerReference ref;
	private Arena arena;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
		ref = new IntegerReference(arena);
	}

	@Test
	void set() {
		ref.set(2);
		assertEquals(2, ref.value());
	}

	@Test
	void equals() {
		assertEquals(ref, ref);
		assertEquals(ref, new IntegerReference(arena));
		assertNotEquals(ref, null);
		final var other = new IntegerReference(arena);
		other.set(1);
		assertNotEquals(ref, other);
	}

	@Nested
	class MapperTests {
		private IntegerReferenceNativeMapper mapper;

		@BeforeEach
		void before() {
			mapper = new IntegerReferenceNativeMapper();
		}

		@Test
		void mapper() {
			assertEquals(IntegerReference.class, mapper.type());
			assertEquals(ADDRESS, mapper.layout());
		}

		@Test
		void toNative() {
			ref.set(3);
			final MemorySegment address = mapper.toNative(ref, arena);
			assertEquals(3, address.get(ValueLayout.JAVA_INT, 0));
		}

		@Test
		void toNativeNull() {
			assertThrows(UnsupportedOperationException.class, () -> mapper.toNativeNull(IntegerReference.class));
		}
	}
}
