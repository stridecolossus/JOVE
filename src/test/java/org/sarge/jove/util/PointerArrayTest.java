package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.util.PointerArray;

import com.sun.jna.Pointer;

public class PointerArrayTest {
	private Pointer array;
	private Pointer ptr;

	@BeforeEach
	void before() {
		ptr = new Pointer(1);
		array = new PointerArray(new Pointer[]{ptr, ptr});
	}

	@Test
	void constructor() {
		assertArrayEquals(new Pointer[]{ptr, ptr}, array.getPointerArray(0, 2));
	}

	@Test
	void equals() {
		assertEquals(array, array);
		assertEquals(array, new PointerArray(new Pointer[]{ptr, ptr}));
		assertNotEquals(array, null);
		assertNotEquals(array, new PointerArray(new Pointer[1]));
	}
}
