package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle.HandleNativeTransformer;
import org.sarge.jove.foreign.NativeTransformer.ParameterMode;

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
		private HandleNativeTransformer transformer;

		@BeforeEach
		void before() {
			transformer = new HandleNativeTransformer();
		}

		@Test
		void layout() {
			assertEquals(ValueLayout.ADDRESS, transformer.layout());
		}

		@DisplayName("A handle can be transformed to a memory address")
		@Test
		void transform() {
			assertEquals(address, transformer.transform(handle, ParameterMode.VALUE, null));
		}

		@DisplayName("An empty handle can be transformed to a null memory address")
		@Test
		void empty() {
			assertEquals(MemorySegment.NULL, transformer.transform(null, ParameterMode.VALUE, null));
		}

		@DisplayName("A handle can be returned by a native method")
		@Test
		void returns() {
			final var returns = transformer.returns();
			assertEquals(new Handle(address), returns.apply(address));
			assertEquals(new Handle(MemorySegment.NULL), returns.apply(MemorySegment.NULL));
		}

		@DisplayName("A handle cannot be returned as a by-reference parameter")
		@Test
		void update() {
			assertThrows(UnsupportedOperationException.class, () -> transformer.update());
		}
	}
}
