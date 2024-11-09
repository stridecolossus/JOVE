package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.lib.Handle.HandleNativeMapper;

class HandleTest {
	private Handle handle;

	@BeforeEach
	void before() {
		handle = new Handle(3);
	}

	@Test
	void address() {
		assertEquals(MemorySegment.ofAddress(3), handle.address());
	}

	@Test
	void equals() {
		assertEquals(handle, handle);
		assertEquals(handle, new Handle(3));
		assertNotEquals(handle, null);
		assertNotEquals(handle, new Handle(4));
	}

	@Nested
	class MapperTests {
		private HandleNativeMapper mapper;
		private Arena arena;

		@BeforeEach
		void before() {
			arena = Arena.ofAuto();
			mapper = new HandleNativeMapper();
		}

		@Test
		void toNative() {
			assertEquals(MemorySegment.ofAddress(3), mapper.toNative(handle, arena));
		}

		@Test
		void toNativeNull() {
			assertEquals(MemorySegment.NULL, mapper.toNativeNull(Handle.class));
		}

		@Test
		void fromNative() {
			assertEquals(handle, mapper.fromNative(MemorySegment.ofAddress(3), Handle.class));
		}
	}
}
