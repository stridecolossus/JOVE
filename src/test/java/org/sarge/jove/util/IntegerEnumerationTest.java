package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.jove.util.IntegerEnumeration.ReverseMapping;

import com.sun.jna.FromNativeContext;

public class IntegerEnumerationTest {
	/**
	 * Mock implementation.
	 */
	private static enum MockEnum implements IntegerEnumeration {
		A(1),
		B(2),
		C(4);

		private final int value;

		private MockEnum(int value) {
			this.value = value;
		}

		@Override
		public int value() {
			return value;
		}
	}

	@Test
	void mapping() {
		final ReverseMapping<MockEnum> mapping = IntegerEnumeration.mapping(MockEnum.class);
		assertNotNull(mapping);
	}

	@Test
	public void map() {
		final var mapping = IntegerEnumeration.mapping(MockEnum.class);
		assertEquals(MockEnum.A, mapping.map(0x01));
		assertEquals(MockEnum.B, mapping.map(0x02));
		assertEquals(MockEnum.C, mapping.map(0x04));
	}

	@Test
	public void mapInvalidLiteral() {
		final var mapping = IntegerEnumeration.mapping(MockEnum.class);
		assertThrows(IllegalArgumentException.class, () -> mapping.map(0));
		assertThrows(IllegalArgumentException.class, () -> mapping.map(999));
	}

	@Test
	public void enumerate() {
		final var mapping = IntegerEnumeration.mapping(MockEnum.class);
		assertEquals(Set.of(), mapping.enumerate(0));
		assertEquals(Set.of(MockEnum.A), mapping.enumerate(0b001));
		assertEquals(Set.of(MockEnum.B), mapping.enumerate(0b010));
		assertEquals(Set.of(MockEnum.C), mapping.enumerate(0b100));
		assertEquals(Set.of(MockEnum.A, MockEnum.C), mapping.enumerate(0b101));
		assertEquals(Set.of(MockEnum.A, MockEnum.B, MockEnum.C), mapping.enumerate(0b111));
	}

	@Test
	public void mask() {
		assertEquals(0, IntegerEnumeration.mask(Set.of()));
		assertEquals(0b001, IntegerEnumeration.mask(Set.of(MockEnum.A)));
		assertEquals(0b010, IntegerEnumeration.mask(Set.of(MockEnum.B)));
		assertEquals(0b100, IntegerEnumeration.mask(Set.of(MockEnum.C)));
		assertEquals(0b101, IntegerEnumeration.mask(Set.of(MockEnum.A, MockEnum.C)));
		assertEquals(0b111, IntegerEnumeration.mask(Set.of(MockEnum.A, MockEnum.B, MockEnum.C)));
	}

	@Nested
	class ConverterTests {
		private FromNativeContext context;

		@BeforeEach
		void before() {
			final Class clazz = MockEnum.class;					// Has to be explicit field and non-generic
			context = mock(FromNativeContext.class);
			when(context.getTargetType()).thenReturn(clazz);
		}

		@Test
		public void nativeType() {
			assertEquals(Integer.class, IntegerEnumeration.CONVERTER.nativeType());
		}

		@Test
		public void toNative() {
			assertEquals(1, IntegerEnumeration.CONVERTER.toNative(MockEnum.A, null));
			assertEquals(2, IntegerEnumeration.CONVERTER.toNative(MockEnum.B, null));
			assertEquals(4, IntegerEnumeration.CONVERTER.toNative(MockEnum.C, null));
		}

		@Test
		public void toNativeNull() {
			assertEquals(0, IntegerEnumeration.CONVERTER.toNative(null, null));
		}

		@Test
		public void fromNative() {
			assertEquals(MockEnum.B, IntegerEnumeration.CONVERTER.fromNative(2, context));
		}

		@Test
		public void fromNativeZero() {
			assertEquals(MockEnum.A, IntegerEnumeration.CONVERTER.fromNative(0, context));
		}

		@Test
		public void fromNativeInvalidClass() {
			final Class clazz = String.class;				// Has to be explicit field and non-generic
			when(context.getTargetType()).thenReturn(clazz);
			assertThrows(RuntimeException.class, () -> IntegerEnumeration.CONVERTER.fromNative(2, context));
		}
	}
}
