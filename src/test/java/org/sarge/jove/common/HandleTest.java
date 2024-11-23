package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.Handle.HandleNativeMapper;
import org.sarge.jove.foreign.*;

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

		@BeforeEach
		void before() {
			mapper = new HandleNativeMapper();
		}

		@Test
		void mapper() {
			assertEquals(Handle.class, mapper.type());
			assertEquals(ValueLayout.ADDRESS, mapper.layout(null));
		}

		@Test
		void marshal() {
			assertEquals(MemorySegment.ofAddress(3), mapper.marshal(handle, new NativeContext()));
		}

		@Test
		void marshalNull() {
			assertEquals(MemorySegment.NULL, mapper.marshalNull(Handle.class));
		}

		@Test
		void unmarshal() {
			assertEquals(handle, mapper.unmarshal(MemorySegment.ofAddress(3), Handle.class));
		}
	}
}
