package org.sarge.jove.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.util.PointerArray;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class NativeObjectTest {
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
		final NativeObject obj = () -> handle;
		final Pointer[] ptrs = Handle.toArray(List.of(obj, obj));
		assertArrayEquals(new Pointer[]{ptr, ptr}, ptrs);
	}

	@Test
	void toPointerArray() {
		final NativeObject obj = () -> handle;
		final PointerArray array = Handle.toPointerArray(List.of(obj, obj));
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
