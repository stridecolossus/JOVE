package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.MockEnum.*;

import java.lang.foreign.ValueLayout;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.EnumMask.EnumMaskTransformer;
import org.sarge.jove.util.IntEnum.ReverseMapping;

class EnumMaskTest {
	private ReverseMapping<MockEnum> mapping;

	@BeforeEach
	void before() {
		mapping = new ReverseMapping<>(MockEnum.class);
	}

	@Test
	void contains() {
		final var mask = new EnumMask<>(A);
		assertEquals(true, mask.contains(mask));
		assertEquals(true, mask.contains(new EnumMask<>()));
		assertEquals(false, mask.contains(new EnumMask<>(B)));
		assertEquals(false, mask.contains(new EnumMask<>(A, B)));
	}

	@DisplayName("A collection of enumeration constants can be reduced to a bitfield")
	@Test
	void bitfield() {
		assertEquals(0, 	new EnumMask<>().bits());
		assertEquals(0b001, new EnumMask<>(A).bits());
		assertEquals(0b010, new EnumMask<>(B).bits());
		assertEquals(0b011, new EnumMask<>(A, B).bits());
	}

	@DisplayName("A bitfield can be converted to the corresponding set of constants")
	@Test
	void enumerate() {
		assertEquals(Set.of(), new EnumMask<MockEnum>(0).enumerate(mapping));
		assertEquals(Set.of(A), new EnumMask<MockEnum>(0b001).enumerate(mapping));
		assertEquals(Set.of(B), new EnumMask<MockEnum>(0b010).enumerate(mapping));
		assertEquals(Set.of(A, B), new EnumMask<MockEnum>(0b011).enumerate(mapping));
	}

	@Test
	void equals() {
		final var mask = new EnumMask<>(A);
		assertEquals(mask, mask);
		assertEquals(mask, new EnumMask<>(A));
		assertNotEquals(mask, null);
		assertNotEquals(mask, new EnumMask<>(B));
	}

	@Nested
	class TransformerTests {
		private EnumMaskTransformer transformer;

		@BeforeEach
		void before() {
			transformer = new EnumMaskTransformer();
		}

		@Test
		void layout() {
			assertEquals(ValueLayout.JAVA_INT, transformer.layout());
		}

		@Test
		void transform() {
			assertEquals(1, transformer.marshal(new EnumMask<>(A), null));
		}

		@Test
		void empty() {
			assertEquals(0, transformer.empty());
		}

		@Test
		void unmarshal() {
			assertEquals(new EnumMask<>(1), transformer.unmarshal().apply(1));
		}

		@Test
		void update() {
			assertThrows(UnsupportedOperationException.class, () -> transformer.update());
		}
	}
}
