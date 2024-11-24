package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle.HandleNativeMapper;
import org.sarge.jove.foreign.NativeContext;

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
			assertEquals(address, mapper.marshal(handle, new NativeContext()));
		}

		@Test
		void marshalNull() {
			assertEquals(MemorySegment.NULL, mapper.marshalNull(Handle.class));
		}

		@Test
		void returns() {
			assertEquals(new Handle(address), mapper.returns(Handle.class).apply(address));
		}

		@Test
		void unmarshal() {
			assertThrows(UnsupportedOperationException.class, () -> mapper.unmarshal(Handle.class));
		}
	}
}
