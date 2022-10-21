package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class HandleTest {
	private Handle handle;
	private Pointer ptr;

	@BeforeEach
	void before() {
		ptr = new Pointer(42);
		handle = new Handle(ptr);
	}

	@Test
	void constructor() {
		assertEquals(ptr.hashCode(), handle.hashCode());
		assertEquals(ptr, handle.pointer());
	}

	@Test
	void of() {
		final var ref = new PointerByReference(new Pointer(42));
		assertEquals(handle, Handle.of(ref));
	}

	@Test
	void equals() {
		assertEquals(true, handle.equals(handle));
		assertEquals(true, handle.equals(new Handle(new Pointer(42))));
		assertEquals(false, handle.equals(null));
		assertEquals(false, handle.equals(new Handle(new Pointer(999))));
	}

	@Nested
	class ConverterTests {
		@Test
		void nativeType() {
			assertEquals(Pointer.class, Handle.CONVERTER.nativeType());
		}

		@Test
		void toNative() {
			assertEquals(ptr, Handle.CONVERTER.toNative(handle, null));
			assertEquals(null, Handle.CONVERTER.toNative(null, null));
		}

		@Test
		void fromNative() {
			assertEquals(handle, Handle.CONVERTER.fromNative(ptr, null));
			assertEquals(null, Handle.CONVERTER.fromNative(null, null));
		}
	}
}
