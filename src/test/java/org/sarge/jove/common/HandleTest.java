package org.sarge.jove.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle.HandleArray;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

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

	@Test
	void ofNullable() {
		assertEquals(handle, Handle.ofNullable(() -> handle));
		assertEquals(null, Handle.ofNullable(null));
	}

	@Test
	void toArray() {
		// Convert native objects to handle array
		final NativeObject obj = () -> handle;
		final HandleArray array = Handle.toArray(List.of(obj, obj));

		// Check array memory
		assertNotNull(array);
		assertEquals(2 * Native.POINTER_SIZE, array.size());
		assertEquals(ptr, array.getPointer(0));
		assertEquals(ptr, array.getPointer(Native.POINTER_SIZE));

		// Check equality
		assertEquals(true, array.equals(array));
		assertEquals(true, array.equals(Handle.toArray(List.of(obj, obj))));
		assertEquals(false, array.equals(null));
		assertEquals(false, array.equals(Handle.toArray(List.of(obj))));
	}

	@Test
	void toArrayEmpty() {
		assertEquals(null, Handle.toArray(List.of()));
	}
}
