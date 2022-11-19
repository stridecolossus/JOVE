package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class MaskTest {
	private Mask mask;

	@BeforeEach
	void before() {
		mask = new Mask(0b101);
	}

	@Test
	void contains() {
		assertEquals(true,   mask.contains(0b101));
		assertEquals(true,   mask.contains(0b100));
		assertEquals(true,   mask.contains(0b001));
		assertEquals(true,   mask.contains(0));
		assertEquals(false,  mask.contains(0b111));
		assertEquals(false,  mask.contains(0b1111));
	}

	@Test
	void stream() {
		assertArrayEquals(new int[]{0, 2}, mask.stream().toArray());
	}

	@Test
	void equals() {
		assertEquals(mask, mask);
		assertEquals(mask, new Mask(0b101));
		assertNotEquals(mask, null);
		assertNotEquals(mask, new Mask(0));
	}

	@Test
	void toInteger() {
		assertEquals(1, Mask.toInteger(0));
		assertEquals(2, Mask.toInteger(1));
		assertEquals(4, Mask.toInteger(2));
		assertEquals(1, Mask.toInteger(32));
	}

	@Test
	void unsignedMaximum() {
		assertEquals(255, Mask.unsignedMaximum(8));
		assertEquals(65535, Mask.unsignedMaximum(16));
		assertEquals(4_294_967_295L, Mask.unsignedMaximum(32));
	}
}
