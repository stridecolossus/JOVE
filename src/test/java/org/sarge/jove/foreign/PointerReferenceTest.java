package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.PointerReference.PointerReferenceMapper;

class PointerReferenceTest {
	private PointerReference ref;
	private Arena arena;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
		ref = new PointerReference();
	}

	@Test
	void equals() {
		assertEquals(ref, ref);
		assertNotEquals(ref, null);
		assertNotEquals(ref, new PointerReference());
	}

	@Nested
	class MapperTests {
		private PointerReferenceMapper mapper;

		@BeforeEach
		void before() {
			mapper = new PointerReferenceMapper();
		}

		@Test
		void mapper() {
			assertEquals(PointerReference.class, mapper.type());
			assertEquals(ADDRESS, mapper.layout());
			assertEquals(mapper, mapper.derive(null));
		}

		@Test
		void marshal() {
			mapper.marshal(ref, arena);
		}

		@Test
		void marshalNull() {
			assertThrows(NullPointerException.class, () -> mapper.marshalNull());
		}

		@Test
		void returns() {
			assertThrows(UnsupportedOperationException.class, () -> mapper.returns());
		}

		@Test
		void unmarshal() {
			final MemorySegment address = mapper.marshal(ref, arena);
			final MemorySegment ptr = arena.allocate(ADDRESS);
			address.set(ADDRESS, 0, ptr);
			mapper.reference().accept(address, ref);
			assertEquals(new Handle(ptr), ref.handle());
		}
	}
}
