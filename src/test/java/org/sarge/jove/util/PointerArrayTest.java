package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.sun.jna.Pointer;

class PointerArrayTest {
	@Test
	void ints() {
		final var array = new PointerToIntArray(new int[]{2, 3});
		assertEquals(2 * 4, array.size());
		assertEquals(array, array);
		assertEquals(array, new PointerToIntArray(new int[]{2, 3}));
		assertNotEquals(array, null);
	}

	@Test
	void floats() {
		final var array = new PointerToFloatArray(new float[]{2, 3});
		assertEquals(2 * 4, array.size());
		assertEquals(array, array);
		assertEquals(array, new PointerToFloatArray(new float[]{2, 3}));
		assertNotEquals(array, null);
	}

	@Test
	void pointers() {
		final var array = new PointerToPointerArray(new Pointer[]{new Pointer(2), new Pointer(3)});
		assertEquals(2 * 8, array.size());
		assertEquals(array, array);
		assertEquals(array, new PointerToPointerArray(new Pointer[]{new Pointer(2), new Pointer(3)}));
		assertNotEquals(array, null);
	}
}
