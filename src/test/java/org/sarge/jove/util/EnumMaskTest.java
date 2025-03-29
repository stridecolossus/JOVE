package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.EnumMask.EnumMaskNativeTransformer;
import org.sarge.jove.util.IntEnum.ReverseMapping;

class EnumMaskTest {
	private ReverseMapping<MockEnum> mapping;

	@BeforeEach
	void before() {
		mapping = IntEnum.reverse(MockEnum.class);
	}

	@Test
	void contains() {
		final var mask = new EnumMask<>(MockEnum.A);
		assertEquals(true, mask.contains(mask));
		assertEquals(true, mask.contains(new EnumMask<>()));
		assertEquals(false, mask.contains(new EnumMask<>(MockEnum.B)));
		assertEquals(false, mask.contains(new EnumMask<>(MockEnum.A, MockEnum.B)));
	}

	@DisplayName("A collection of enumeration constants can be reduced to a bitfield")
	@Test
	void bitfield() {
		assertEquals(0, 	new EnumMask<>().bits());
		assertEquals(0b001, new EnumMask<>(MockEnum.A).bits());
		assertEquals(0b010, new EnumMask<>(MockEnum.B).bits());
		assertEquals(0b011, new EnumMask<>(MockEnum.A, MockEnum.B).bits());
	}

	@DisplayName("A bitfield can be converted to the corresponding set of constants")
	@Test
	void enumerate() {
		assertEquals(Set.of(), new EnumMask<MockEnum>(0).enumerate(mapping));
		assertEquals(Set.of(MockEnum.A), new EnumMask<MockEnum>(0b001).enumerate(mapping));
		assertEquals(Set.of(MockEnum.B), new EnumMask<MockEnum>(0b010).enumerate(mapping));
		assertEquals(Set.of(MockEnum.A, MockEnum.B), new EnumMask<MockEnum>(0b011).enumerate(mapping));
	}

	@Test
	void equals() {
		final var mask = new EnumMask<>(MockEnum.A);
		assertEquals(mask, mask);
		assertEquals(mask, new EnumMask<>(MockEnum.A));
		assertNotEquals(mask, null);
		assertNotEquals(mask, new EnumMask<>(MockEnum.B));
	}

	@Nested
	class TransformerTests {
		private EnumMaskNativeTransformer transformer;

		@BeforeEach
		void before() {
			transformer = new EnumMaskNativeTransformer();
		}

		@Test
		void layout() {
			assertEquals(ValueLayout.JAVA_INT, transformer.layout());
		}

		@Test
		void transform() {
			assertEquals(1, transformer.marshal(new EnumMask<>(MockEnum.A), null));
		}

		@Test
		void empty() {
			assertEquals(0, transformer.empty());
		}

		@Test
		void returns() {
			assertEquals(new EnumMask<>(1), transformer.unmarshal().apply(1));
		}
	}
}
