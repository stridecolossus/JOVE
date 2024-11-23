package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.IntEnum.ReverseMapping;

class BitMaskTest {
	private ReverseMapping<MockEnum> mapping;

	@BeforeEach
	void before() {
		mapping = IntEnum.reverse(MockEnum.class);
	}

	@Test
	void contains() {
		final BitMask<MockEnum> mask = BitMask.of(MockEnum.A);
		assertEquals(true, mask.contains(mask));
		assertEquals(true, mask.contains(BitMask.of()));
		assertEquals(false, mask.contains(BitMask.of(MockEnum.B)));
		assertEquals(false, mask.contains(BitMask.of(MockEnum.A, MockEnum.B)));
	}

	@DisplayName("A collection of enumeration constants can be reduced to a bitfield")
	@Test
	void bitfield() {
		assertEquals(0, 	BitMask.of().bits());
		assertEquals(0b001, BitMask.of(MockEnum.A).bits());
		assertEquals(0b010, BitMask.of(MockEnum.B).bits());
		assertEquals(0b011, BitMask.of(MockEnum.A, MockEnum.B).bits());
	}

	@DisplayName("A bitfield can be converted to the corresponding set of constants")
	@Test
	void enumerate() {
		assertEquals(Set.of(), new BitMask<MockEnum>(0).enumerate(mapping));
		assertEquals(Set.of(MockEnum.A), new BitMask<MockEnum>(0b001).enumerate(mapping));
		assertEquals(Set.of(MockEnum.B), new BitMask<MockEnum>(0b010).enumerate(mapping));
		assertEquals(Set.of(MockEnum.A, MockEnum.B), new BitMask<MockEnum>(0b011).enumerate(mapping));
	}

	@Test
	void equals() {
		final BitMask<MockEnum> mask = BitMask.of(MockEnum.A);
		assertEquals(mask, mask);
		assertEquals(mask, BitMask.of(MockEnum.A));
		assertNotEquals(mask, null);
		assertNotEquals(mask, BitMask.of(MockEnum.B));
	}
}
