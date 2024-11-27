package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.PointerReference.PointerReferenceTransform;

class PointerReferenceTest {
	private PointerReference ref;
	private Arena arena;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
		ref = new PointerReference();
	}

	@Test
	void unitialised() {
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
		private PointerReferenceTransform transformer;

		@BeforeEach
		void before() {
			transformer = new PointerReferenceTransform();
		}

		@Test
		void constructor() {
			assertEquals(PointerReference.class, transformer.type());
			assertEquals(ADDRESS, transformer.layout());
			assertEquals(transformer, transformer.derive(null, null));
		}

		@Test
		void transform() {
			transformer.transform(ref, arena);
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
			final MemorySegment address = transformer.transform(ref, arena);
			final MemorySegment ptr = arena.allocate(ADDRESS);
			address.set(ADDRESS, 0, ptr);
			transformer.update().accept(address, ref);
			assertEquals(new Handle(ptr), ref.handle());
		}
	}
}
