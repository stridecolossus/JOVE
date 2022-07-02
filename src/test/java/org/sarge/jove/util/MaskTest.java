package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class MaskTest {
	private Mask mask;

	@BeforeEach
	void before() {
		mask = new Mask(0b101);
	}

	@Test
	void contains() {
		assertEquals(true,  mask.contains(0b101));
		assertEquals(true,  mask.contains(0b100));
		assertEquals(true,  mask.contains(0b001));
		assertEquals(true,  mask.contains(0));
		assertEquals(false,  mask.contains(0b111));
		assertEquals(false,  mask.contains(0b1111));
	}

	@Test
	void bit() {

//		System.out.println(Integer.highestOneBit(0));
//		System.out.println(Integer.highestOneBit(0b001));
//		System.out.println(Integer.highestOneBit(0b010));
//		System.out.println(Integer.highestOneBit(0b100));

		assertEquals(true, mask.bit(0));
		assertEquals(false, mask.bit(1));
		assertEquals(true, mask.bit(2));
		assertEquals(false, mask.bit(4));
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

	@SuppressWarnings("static-method")
	@Test
	void unsignedMaximum() {
		assertEquals(255, Mask.unsignedMaximum(8));
		assertEquals(65535, Mask.unsignedMaximum(16));
		assertEquals(4_294_967_295L, Mask.unsignedMaximum(32));
	}
}
