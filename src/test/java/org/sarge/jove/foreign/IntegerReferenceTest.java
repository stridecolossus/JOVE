package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.foreign.IntegerReference.IntegerReferenceNativeMapper;

class IntegerReferenceTest {
	private IntegerReference ref;

	@BeforeEach
	void before() {
		ref = new IntegerReference();
	}

//	@Test
//	void set() {
//		ref.set(2);
//		assertEquals(2, ref.value());
//	}

	@Test
	void equals() {
		assertEquals(ref, ref);
		assertEquals(ref, new IntegerReference());
		assertNotEquals(ref, null);
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
			assertEquals(ValueLayout.ADDRESS, mapper.layout(null));
		}

		@Test
		void toNative() {
			final MemorySegment address = mapper.marshal(ref, new NativeContext());
			ref.set(3);
			assertEquals(3, address.get(JAVA_INT, 0));
		}

		@Test
		void toNativeNull() {
			assertThrows(UnsupportedOperationException.class, () -> mapper.marshalNull(null));
		}
	}
}
