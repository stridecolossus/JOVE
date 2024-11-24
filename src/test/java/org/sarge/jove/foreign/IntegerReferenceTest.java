package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.IntegerReference.IntegerReferenceMapper;

class IntegerReferenceTest {
	private IntegerReference ref;

	@BeforeEach
	void before() {
		ref = new IntegerReference();
	}

	@Test
	void def() {
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
			assertEquals(ADDRESS, mapper.layout(null));
		}

		@Test
		void marshal() {
			ref.set(3);
			final MemorySegment address = mapper.marshal(ref, new NativeContext());
			assertEquals(3, address.get(JAVA_INT, 0));
		}

		@Test
		void marshalNull() {
			assertThrows(UnsupportedOperationException.class, () -> mapper.marshalNull(IntegerReference.class));
		}

		@Test
		void returns() {
			assertThrows(UnsupportedOperationException.class, () -> mapper.returns(IntegerReference.class));
		}

		@Test
		void unmarshal() {
			final MemorySegment address = Arena.ofAuto().allocate(ADDRESS);
			address.set(JAVA_INT, 0, 4);
			mapper.unmarshal(IntegerReference.class).accept(address, ref);
			assertEquals(4, ref.value());
		}
	}
}
