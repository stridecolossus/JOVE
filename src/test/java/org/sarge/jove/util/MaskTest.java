package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

@SuppressWarnings("static-method")
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
	void matches() {
		assertEquals(true, 	mask.matches(0b111));
		assertEquals(true,  mask.matches(0b101));
		assertEquals(false, mask.matches(0b100));
		assertEquals(false, mask.matches(0b001));
		assertEquals(false,	mask.matches(0b010));
		assertEquals(false, mask.matches(0));
	}

	@Test
	void stream() {
		assertNotNull(mask.stream());
		assertArrayEquals(new int[]{0b001, 0b100}, mask.stream().toArray());
	}

	@Test
	void equals() {
		assertEquals(mask, mask);
		assertEquals(mask, new Mask(0b101));
		assertNotEquals(mask, null);
		assertNotEquals(mask, new Mask(0));
	}

	@Test
	void index() {
		assertEquals(0b001, Mask.index(0));
		assertEquals(0b010, Mask.index(1));
		assertEquals(0b100, Mask.index(2));
	}

	@Test
	void unsignedMaximum() {
		assertEquals(255, Mask.unsignedMaximum(8));
		assertEquals(65535, Mask.unsignedMaximum(16));
		assertEquals(4_294_967_295L, Mask.unsignedMaximum(32));
	}
}
