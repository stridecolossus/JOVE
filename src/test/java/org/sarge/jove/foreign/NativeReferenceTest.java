package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeReference.*;

class NativeReferenceTest {
	private Factory factory;

	@BeforeEach
	void before() {
		factory = new Factory();
	}

	@Test
	void integer() {
		final var integer = factory.integer();
		assertEquals(0, integer.get());
	}

	@Test
	void pointer() {
		final var pointer = factory.pointer();
		assertEquals(null, pointer.get());
	}

	@Nested
	class TransformerTests {
		private NativeReferenceTransformer transformer;
		private NativeReference<Integer> ref;

		@BeforeEach
		void before() {
			transformer = new NativeReferenceTransformer();
			ref = factory.integer();
		}

    	@Test
    	void marshal() {
    		assertNotNull(transformer.marshal(ref, null));
    	}

    	@Test
    	void reference() {
    		final MemorySegment address = (MemorySegment) transformer.marshal(ref, null);
    		address.set(ValueLayout.JAVA_INT, 0L, 42);
    		assertEquals(42, ref.get());
    	}

    	@Test
    	void unmarshal() {
    		assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal());
    	}
    }
}
