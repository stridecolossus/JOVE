package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.IntegerEnumeration.ReverseMapping;

class BitFieldTest {
	private ReverseMapping<MockEnum> mapping;

	@BeforeEach
	void before() {
		mapping = IntegerEnumeration.reverse(MockEnum.class);
	}

	@DisplayName("A collection of enumeration constants can be reduced to a bitfield")
	@Test
	void reduce() {
		assertEquals(new BitField<>(0), 	BitField.reduce(Set.of()));
		assertEquals(new BitField<>(0b001), BitField.reduce(MockEnum.A));
		assertEquals(new BitField<>(0b010), BitField.reduce(MockEnum.B));
		assertEquals(new BitField<>(0b011), BitField.reduce(MockEnum.A, MockEnum.B));
	}

	@DisplayName("A bitfield can be converted to the corresponding set of constants")
	@Test
	void enumerate() {
		assertEquals(Set.of(), new BitField<MockEnum>(0).enumerate(mapping));
		assertEquals(Set.of(MockEnum.A), new BitField<MockEnum>(0b001).enumerate(mapping));
		assertEquals(Set.of(MockEnum.B), new BitField<MockEnum>(0b010).enumerate(mapping));
		assertEquals(Set.of(MockEnum.A, MockEnum.B), new BitField<MockEnum>(0b011).enumerate(mapping));
	}

	@Test
	void equals() {
		final BitField<MockEnum> bitfield = new BitField<>(1);
		assertEquals(bitfield, bitfield);
		assertEquals(bitfield, new BitField<MockEnum>(1));
		assertNotEquals(bitfield, null);
		assertNotEquals(bitfield, new BitField<>(2));
	}

	@Nested
	class ConverterTests {
		@Test
		void type() {
			assertEquals(Integer.class, BitField.CONVERTER.nativeType());
		}

		@Test
		void toNative() {
			assertEquals(1, BitField.CONVERTER.toNative(new BitField<>(1), null));
		}

		@Test
		void fromNative() {
			assertEquals(new BitField<>(1), BitField.CONVERTER.fromNative(1, null));
		}
	}
}
