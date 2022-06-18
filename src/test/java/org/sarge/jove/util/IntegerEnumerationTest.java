package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
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

	private ReverseMapping<MockEnum> mapping;

	@BeforeEach
	void before() {
		mapping = IntegerEnumeration.mapping(MockEnum.class);
	}

	@Test
	void constructor() {
		assertNotNull(mapping);
	}

	@DisplayName("A native value can be mapped to a valid enumeration constant")
	@Test
	void map() {
		assertEquals(MockEnum.A, mapping.map(1));
		assertEquals(MockEnum.B, mapping.map(2));
		assertEquals(MockEnum.C, mapping.map(4));
	}

	@DisplayName("An invalid native cannot be mapped to an enumeration constant")
	@Test
	void mapInvalidLiteral() {
		assertThrows(IllegalArgumentException.class, () -> mapping.map(0));
		assertThrows(IllegalArgumentException.class, () -> mapping.map(999));
	}

	@DisplayName("An arbitrary collection of constants can be reduced to an integer bit-field")
	@SuppressWarnings("static-method")
	@Test
	void reduce() {
		assertEquals(0, IntegerEnumeration.reduce(Set.of()));
		assertEquals(0b001, IntegerEnumeration.reduce(Set.of(MockEnum.A)));
		assertEquals(0b010, IntegerEnumeration.reduce(Set.of(MockEnum.B)));
		assertEquals(0b100, IntegerEnumeration.reduce(Set.of(MockEnum.C)));
		assertEquals(0b101, IntegerEnumeration.reduce(Set.of(MockEnum.A, MockEnum.C)));
		assertEquals(0b111, IntegerEnumeration.reduce(Set.of(MockEnum.A, MockEnum.B, MockEnum.C)));
	}

	@DisplayName("A bit-field can be transformed to the corresponding set of constants")
	@Test
	void enumerate() {
		assertEquals(Set.of(), mapping.enumerate(0));
		assertEquals(Set.of(MockEnum.A), mapping.enumerate(0b001));
		assertEquals(Set.of(MockEnum.B), mapping.enumerate(0b010));
		assertEquals(Set.of(MockEnum.C), mapping.enumerate(0b100));
		assertEquals(Set.of(MockEnum.A, MockEnum.C), mapping.enumerate(0b101));
		assertEquals(Set.of(MockEnum.A, MockEnum.B, MockEnum.C), mapping.enumerate(0b111));
	}

	@Nested
	class ConverterTests {
		private FromNativeContext context;

		@SuppressWarnings({"unchecked", "rawtypes"})
		@BeforeEach
		void before() {
			final Class clazz = MockEnum.class;					// Has to be explicit field and non-generic
			context = mock(FromNativeContext.class);
			when(context.getTargetType()).thenReturn(clazz);
		}

		@DisplayName("An integer enumeration is represented natively by an unsigned integer")
		@Test
		void nativeType() {
			assertEquals(Integer.class, IntegerEnumeration.CONVERTER.nativeType());
		}

		@DisplayName("An enumeration constant can be converted to its native representation")
		@Test
		void toNative() {
			assertEquals(1, IntegerEnumeration.CONVERTER.toNative(MockEnum.A, null));
			assertEquals(2, IntegerEnumeration.CONVERTER.toNative(MockEnum.B, null));
			assertEquals(4, IntegerEnumeration.CONVERTER.toNative(MockEnum.C, null));
		}

		@DisplayName("A NULL constant cannot be converted")
		@Test
		void toNativeNull() {
			assertEquals(0, IntegerEnumeration.CONVERTER.toNative(null, null));
		}

		@DisplayName("A native value can be converted to the corresponding constant")
		@Test
		void fromNative() {
			assertEquals(MockEnum.B, IntegerEnumeration.CONVERTER.fromNative(2, context));
		}

		@DisplayName("An undefined native value is converted to the 'zero' constant")
		@Test
		void fromNativeZero() {
			assertEquals(MockEnum.A, IntegerEnumeration.CONVERTER.fromNative(0, context));
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		@Test
		void fromNativeInvalidClass() {
			final Class clazz = String.class;				// Has to be explicit field and non-generic
			when(context.getTargetType()).thenReturn(clazz);
			assertThrows(RuntimeException.class, () -> IntegerEnumeration.CONVERTER.fromNative(2, context));
		}
	}
}
