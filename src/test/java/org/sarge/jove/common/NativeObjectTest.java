package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
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

	@Nested
	class TransformerTest {
		private NativeObjectTransformer transformer;

		@BeforeEach
		void before() {
			transformer = new NativeObjectTransformer();
		}

		@Test
    	void layout() {
			assertEquals(ValueLayout.ADDRESS, transformer.layout());
		}

		@Test
    	void marshal() {
    		assertEquals(object.handle.address(), transformer.marshal(object, null));
		}

		@Test
		void empty() {
			assertEquals(MemorySegment.NULL, transformer.empty());
		}

		@Test
		void unmarshal() {
    		assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal());
    	}

		@Test
		void update() {
    		assertThrows(UnsupportedOperationException.class, () -> transformer.update());
    	}
    }
}
