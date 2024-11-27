package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.IntegerReference.IntegerReferenceMapper;

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
		private IntegerReferenceMapper mapper;

		@BeforeEach
		void before() {
			mapper = new IntegerReferenceMapper();
		}

		@Test
		void mapper() {
			assertEquals(IntegerReference.class, mapper.type());
			assertEquals(ADDRESS, mapper.layout());
		}

		@Test
		void marshal() {
			ref.set(3);
			final MemorySegment address = mapper.marshal(ref, arena);
			assertEquals(3, address.get(JAVA_INT, 0));
		}

		@Test
		void empty() {
			assertThrows(NullPointerException.class, () -> mapper.empty());
		}

		@Test
		void returns() {
			assertThrows(UnsupportedOperationException.class, () -> mapper.returns());
		}

		@Test
		void unmarshal() {
			final MemorySegment address = arena.allocate(ADDRESS);
			address.set(JAVA_INT, 0, 4);

			final var other = new IntegerReference();
			mapper.marshal(other, arena);
			mapper.reference().accept(address, other);
			assertEquals(4, other.value());
		}
	}
}
