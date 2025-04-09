package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle.HandleTransformer;
import org.sarge.jove.foreign.NativeReference;

class HandleTest {
	private Handle handle;
	private MemorySegment address;

	@BeforeEach
	void before() {
		address = MemorySegment.ofAddress(2);
		handle = new Handle(2);
	}

	@Test
	void address() {
		assertEquals(address, handle.address());
	}

	@Test
	void invalid() {
		assertThrows(NullPointerException.class, () -> new Handle(MemorySegment.NULL));
	}

	@Test
	void equals() {
		assertEquals(handle, handle);
		assertEquals(handle, new Handle(2));
		assertNotEquals(handle, null);
		assertNotEquals(handle, new Handle(3));
	}

	@Nested
	class ReferenceTests {
		private NativeReference<Handle> ref;

		@BeforeEach
		void before() {
    		ref = Handle.reference();
		}

		@Test
    	void empty() {
    		assertEquals(null, ref.get());
    	}

		@Test
		void set() {
			ref.set(handle);
			assertEquals(handle, ref.get());
		}
	}

	@Nested
	class TransformerTests {
		private HandleTransformer transformer;

		@BeforeEach
		void before() {
			transformer = new HandleTransformer();
		}

		@Test
    	void marshal() {
    		assertEquals(address, transformer.marshal(handle, null));
    	}

    	@Test
    	void unmarshal() {
    		assertEquals(handle, transformer.unmarshal(address));
    	}
    }
}
