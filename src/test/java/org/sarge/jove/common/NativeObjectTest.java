package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.NativeObject.NativeObjectTransformer;
import org.sarge.jove.foreign.NativeTransformer.ParameterMode;

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
	class TransformerTests {
		private NativeObjectTransformer transformer;

		@BeforeEach
		void before() {
			transformer = new NativeObjectTransformer();
		}

		@Test
		void layout() {
			assertEquals(ValueLayout.ADDRESS, transformer.layout());
		}

		@DisplayName("A native object is transformed to its memory address")
		@Test
		void transform() {
			assertEquals(object.handle.address(), transformer.transform(object, ParameterMode.VALUE, null));
		}

		@DisplayName("A empty native object is transformed to a null memory address")
		@Test
		void empty() {
			assertEquals(MemorySegment.NULL, transformer.transform(null, ParameterMode.VALUE, null));
		}

		@DisplayName("A native object cannot be returned from a native method")
		@Test
		void returns() {
			assertThrows(UnsupportedOperationException.class, () -> transformer.returns());
		}

		@DisplayName("A native object cannot be updated as a by-reference parameter")
		@Test
		void update() {
			assertThrows(UnsupportedOperationException.class, () -> transformer.update());
		}
	}
}
