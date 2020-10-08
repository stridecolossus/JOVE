package org.sarge.jove.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.util.PointerArray;

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

	@Test
	void toArray() {
		final Pointer[] ptrs = Handle.toArray(List.of(handle, handle), Function.identity());
		assertArrayEquals(new Pointer[]{ptr, ptr}, ptrs);
	}

	@Test
	void toPointerArray() {
		final PointerArray array = Handle.toPointerArray(List.of(handle, handle));
		assertNotNull(array);
		assertEquals(2 * Native.POINTER_SIZE, array.size());
		assertEquals(ptr, array.getPointer(0));
	}

	@Test
	void equals() {
		assertEquals(true, handle.equals(handle));
		assertEquals(true, handle.equals(new Handle(new Pointer(42))));
		assertEquals(false, handle.equals(null));
		assertEquals(false, handle.equals(new Handle(new Pointer(999))));
	}
}
