package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.PointerReference.PointerReferenceMapper;

class PointerReferenceTest {
	private PointerReference ref;

	@BeforeEach
	void before() {
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
			assertEquals(ADDRESS, mapper.layout(null));
		}

		@Test
		void marshal() {
			mapper.marshal(ref, new NativeContext());
		}

		@Test
		void marshalNull() {
			assertThrows(UnsupportedOperationException.class, () -> mapper.marshalNull(PointerReference.class));
		}

		@Test
		void returns() {
			assertThrows(UnsupportedOperationException.class, () -> mapper.returns(PointerReference.class));
		}

		@Test
		void unmarshal() {
			final MemorySegment address = mapper.marshal(ref, new NativeContext());
			final MemorySegment ptr = Arena.ofAuto().allocate(ADDRESS);
			address.set(ADDRESS, 0, ptr);
			mapper.unmarshal(PointerReference.class).accept(address, ref);
			assertEquals(new Handle(ptr), ref.handle());
		}
	}
}
