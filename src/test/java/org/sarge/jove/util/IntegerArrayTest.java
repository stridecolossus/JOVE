package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import com.sun.jna.Pointer;

public class IntegerArrayTest {
	private static final int[] ARRAY = {1, 2, 3};

	private Pointer ptr;

	@BeforeEach
	void before() {
		ptr = new IntegerArray(ARRAY);
	}

	@Test
	void constructor() {
		assertArrayEquals(ARRAY, ptr.getIntArray(0, ARRAY.length));
	}

	@Test
	void equals() {
		assertEquals(ptr, ptr);
		assertEquals(ptr, new IntegerArray(ARRAY));
		assertNotEquals(ptr, null);
		assertNotEquals(ptr, new IntegerArray(new int[1]));
	}
}
