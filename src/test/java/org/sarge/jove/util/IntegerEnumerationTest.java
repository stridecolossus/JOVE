package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.IntegerEnumeration.ReverseMapping;

import com.sun.jna.FromNativeContext;

class IntegerEnumerationTest {
	private ReverseMapping<MockEnum> mapping;

	@BeforeEach
	void before() {
		mapping = IntegerEnumeration.reverse(MockEnum.class);
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

	@DisplayName("An invalid native value cannot be mapped to an enumeration constant")
	@Test
	void mapInvalidLiteral() {
		assertThrows(IllegalArgumentException.class, () -> mapping.map(0));
		assertThrows(IllegalArgumentException.class, () -> mapping.map(999));
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

		@DisplayName("An undefined native value is converted to the 'default' constant")
		@Test
		void fromNativeZero() {
			assertEquals(MockEnum.A, IntegerEnumeration.CONVERTER.fromNative(null, context));
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
