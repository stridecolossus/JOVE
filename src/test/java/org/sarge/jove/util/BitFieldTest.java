package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class BitFieldTest {
	private BitField bitfield;

	@BeforeEach
	void before() {
		bitfield = new BitField(0b101);
	}

	@Test
	void contains() {
		assertEquals(true,   bitfield.contains(0b101));
		assertEquals(true,   bitfield.contains(0b100));
		assertEquals(true,   bitfield.contains(0b001));
		assertEquals(true,   bitfield.contains(0));
		assertEquals(false,  bitfield.contains(0b111));
		assertEquals(false,  bitfield.contains(0b1111));
	}

	@Test
	void stream() {
		assertArrayEquals(new int[]{1, 4}, bitfield.stream().toArray());
	}

	@Test
	void map() {
		assertEquals(1, BitField.map(0));
		assertEquals(2, BitField.map(1));
		assertEquals(4, BitField.map(2));
	}

	@Test
	void overflow() {
		assertEquals(1, BitField.map(32));
	}

	@Test
	void equals() {
		assertEquals(bitfield, bitfield);
		assertEquals(bitfield, new BitField(0b101));
		assertNotEquals(bitfield, null);
		assertNotEquals(bitfield, new BitField(0));
	}

	@Test
	void string() {
		assertEquals(Integer.toBinaryString(bitfield.mask()), bitfield.toString());
	}

	@Test
	void unsignedMaximum() {
		assertEquals(255, BitField.unsignedMaximum(8));
		assertEquals(65535, BitField.unsignedMaximum(16));
		assertEquals(4_294_967_295L, BitField.unsignedMaximum(32));
	}
}
