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
		final BitMask<MockEnum> mask = BitMask.reduce(MockEnum.A);
		assertEquals(true, mask.contains(mask));
		assertEquals(false, mask.contains(BitMask.reduce(MockEnum.B)));
		assertEquals(false, mask.contains(BitMask.reduce(MockEnum.A, MockEnum.B)));
	}

	@DisplayName("A collection of enumeration constants can be reduced to a bitfield")
	@Test
	void reduce() {
		assertEquals(new BitMask<>(0), 	BitMask.reduce(Set.of()));
		assertEquals(new BitMask<>(0b001), BitMask.reduce(MockEnum.A));
		assertEquals(new BitMask<>(0b010), BitMask.reduce(MockEnum.B));
		assertEquals(new BitMask<>(0b011), BitMask.reduce(MockEnum.A, MockEnum.B));
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
		final BitMask<MockEnum> mask = new BitMask<>(1);
		assertEquals(mask, mask);
		assertEquals(mask, new BitMask<MockEnum>(1));
		assertNotEquals(mask, null);
		assertNotEquals(mask, new BitMask<>(2));
	}

	@Nested
	class ConverterTests {
		@Test
		void type() {
			assertEquals(Integer.class, BitMask.CONVERTER.nativeType());
		}

		@Test
		void toNative() {
			assertEquals(1, BitMask.CONVERTER.toNative(new BitMask<>(1), null));
		}

		@Test
		void fromNative() {
			assertEquals(new BitMask<>(1), BitMask.CONVERTER.fromNative(1, null));
		}
	}
}
