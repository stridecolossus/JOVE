package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle.HandleTransformer;

class HandleTest {
	private Handle handle;
	private MemorySegment address;

	@BeforeEach
	void before() {
		address = MemorySegment.ofAddress(42);
		handle = new Handle(42);
	}

	@Test
	void address() {
		assertEquals(address, handle.address());
	}

	@Test
	void equals() {
		assertEquals(handle, handle);
		assertEquals(handle, new Handle(42));
		assertNotEquals(handle, null);
		assertNotEquals(handle, new Handle(999));
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
    		assertEquals(handle, transformer.unmarshal().apply(address));
    	}
    }
}
