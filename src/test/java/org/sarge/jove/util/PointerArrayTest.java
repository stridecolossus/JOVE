package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.jna.Pointer;

public class PointerArrayTest {
	private PointerArray array;

	@BeforeEach
	void before() {
		array = new PointerArray(new Pointer[]{new Pointer(1), new Pointer(2)});
	}

	@Test
	void constructor() {
		assertEquals(2 * 8, array.size());
	}

	@Test
	void read() {
		array.read();
	}
}
