package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle.HandleNativeTransformer;

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

	@Test
	void transformer() {
		final var transformer = new HandleNativeTransformer();
		assertEquals(address, transformer.marshal(handle, null));
		assertEquals(handle, transformer.unmarshal().apply(address));
	}
}
