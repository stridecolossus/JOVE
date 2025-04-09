package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;

class NativeReferenceTest {
	private NativeReference<Integer> integer;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		final var factory = new NativeReference.Factory();
		integer = factory.integer();
		allocator = Arena.ofAuto();
	}

	@Test
	void get() {
		assertEquals(0, integer.get());
	}

	@Test
	void set() {
		integer.set(42);
		assertEquals(42, integer.get());
	}

	@Nested
	class TransformerTests {
		private NativeReferenceTransformer transformer;

		@BeforeEach
		void before() {
			transformer = new NativeReferenceTransformer();
		}

    	@Test
    	void reference() {
    		final MemorySegment address = transformer.marshal(integer, allocator);
    		address.set(ValueLayout.JAVA_INT, 0L, 42);
    		assertEquals(42, integer.get());
    	}

    	@Test
    	void unmarshal() {
    		assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal(null));
    	}
    }
}
