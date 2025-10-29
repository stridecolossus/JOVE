package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.NativeObject.NativeObjectTransformer;

class NativeObjectTest {
	private static class MockNativeObject implements NativeObject {
		private final Handle handle = new Handle(42);

		@Override
		public Handle handle() {
			return handle;
		}
	}

	private MockNativeObject object;

	@BeforeEach
	void before() {
		object = new MockNativeObject();
	}

	@Test
	void handles() {
		assertArrayEquals(new Handle[]{object.handle}, NativeObject.handles(List.of(object)));
	}

	@Test
	void transformer() {
		final var transformer = new NativeObjectTransformer();
		assertEquals(object.handle.address(), transformer.marshal(object, null));
		assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal(null));
	}
}
