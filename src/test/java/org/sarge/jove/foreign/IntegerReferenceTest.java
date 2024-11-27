package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.IntegerReference.IntegerReferenceTransform;

class IntegerReferenceTest {
	private IntegerReference ref;
	private Arena arena;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
		ref = new IntegerReference();
	}

	@Test
	void value() {
		assertEquals(0, ref.value());
	}

	@Test
	void equals() {
		assertEquals(ref, ref);
		assertEquals(ref, new IntegerReference());
		assertNotEquals(ref, null);
	}

	@Nested
	class MapperTests {
		private IntegerReferenceTransform transformer;

		@BeforeEach
		void before() {
			transformer = new IntegerReferenceTransform();
		}

		@Test
		void constructor() {
			assertEquals(IntegerReference.class, transformer.type());
			assertEquals(ADDRESS, transformer.layout());
		}

		@Test
		void transform() {
			ref.set(3);
			final MemorySegment address = transformer.transform(ref, arena);
			assertEquals(3, address.get(JAVA_INT, 0));
		}

		@Test
		void empty() {
			assertThrows(NullPointerException.class, () -> transformer.empty());
		}

		@Test
		void returns() {
			assertThrows(UnsupportedOperationException.class, () -> transformer.returns());
		}

		@Test
		void update() {
			final MemorySegment address = arena.allocate(ADDRESS);
			address.set(JAVA_INT, 0, 4);

			final var other = new IntegerReference();
			transformer.transform(other, arena);
			transformer.update().accept(address, other);
			assertEquals(4, other.value());
		}
	}
}
