package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.sun.jna.FromNativeContext;

public class IntegerEnumerationTest {
	/**
	 * Mock implementation.
	 */
	enum MockEnum implements IntegerEnumeration {
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
	public void map() {
		assertEquals(MockEnum.A, IntegerEnumeration.map(MockEnum.class, 0x01));
		assertEquals(MockEnum.B, IntegerEnumeration.map(MockEnum.class, 0x02));
		assertEquals(MockEnum.C, IntegerEnumeration.map(MockEnum.class, 0x04));
	}

	@Test
	public void mapInvalidLiteral() {
		assertThrows(IllegalArgumentException.class, () -> IntegerEnumeration.map(MockEnum.class, 0));
		assertThrows(IllegalArgumentException.class, () -> IntegerEnumeration.map(MockEnum.class, 999));
	}

	@Test
	public void enumerate() {
		assertEquals(Set.of(), IntegerEnumeration.enumerate(MockEnum.class, 0));
		assertEquals(Set.of(MockEnum.A), IntegerEnumeration.enumerate(MockEnum.class, 0b001));
		assertEquals(Set.of(MockEnum.B), IntegerEnumeration.enumerate(MockEnum.class, 0b010));
		assertEquals(Set.of(MockEnum.C), IntegerEnumeration.enumerate(MockEnum.class, 0b100));
		assertEquals(Set.of(MockEnum.A, MockEnum.C), IntegerEnumeration.enumerate(MockEnum.class, 0b101));
		assertEquals(Set.of(MockEnum.A, MockEnum.B, MockEnum.C), IntegerEnumeration.enumerate(MockEnum.class, 0b111));
		// TODO - check order
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

	@Test
	public void maskOperator() {
		assertEquals(1 | 2, IntegerEnumeration.MASK.applyAsInt(1, 2));
	}

	@Nested
	class ConverterTests {
		private FromNativeContext context;

		@BeforeEach
		void before() {
			context = mock(FromNativeContext.class);
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

		@SuppressWarnings({"rawtypes", "unchecked"})
		@Test
		public void fromNative() {
			final Class clazz = MockEnum.class; // Has to be explicit field and non-generic
			when(context.getTargetType()).thenReturn(clazz);
			assertEquals(MockEnum.B, IntegerEnumeration.CONVERTER.fromNative(2, context));
		}

		public void fromNativeZero() {
			assertEquals(MockEnum.A, IntegerEnumeration.CONVERTER.fromNative(0, context));
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		@Test
		public void fromNativeInvalidClass() {
			final Class clazz = String.class; // Has to be explicit field and non-generic
			when(context.getTargetType()).thenReturn(clazz);
			assertThrows(IllegalStateException.class, () -> IntegerEnumeration.CONVERTER.fromNative(2, context));
		}
	}
}
