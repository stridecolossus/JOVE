package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout.Type;

public class LayoutTest {
	private Layout component;

	@BeforeEach
	void before() {
		component = new Layout(3, Type.FLOAT, false, Float.BYTES);
	}

	@Test
	void constructor() {
		assertEquals(3, component.count());
		assertEquals(Type.FLOAT, component.type());
		assertEquals(false, component.signed());
		assertEquals(Float.BYTES, component.bytes());
	}

	@DisplayName("A layout has a stride in bytes")
	@Test
	void stride() {
		assertEquals(3 * Float.BYTES, component.stride());
	}

	@DisplayName("A layout has a human-readable representation")
	@Test
	void string() {
		assertEquals("3-FLOAT4U", component.toString());
	}

	@DisplayName("A floating-point layout can be conveniently created")
	@Test
	void floats() {
		assertEquals(new Layout(3, Type.FLOAT, true, Float.BYTES), Layout.floats(3));
	}

	@Nested
	class TypeTests {
		@Test
		void floats() {
			assertEquals(new Layout(1, Type.FLOAT, true, Float.BYTES), Layout.of(Float.class));
			assertEquals(new Layout(1, Type.FLOAT, true, Float.BYTES), Layout.of(float.class));
		}

		@Test
		void longs() {
			assertEquals(new Layout(1, Type.INTEGER, true, Long.BYTES), Layout.of(Long.class));
			assertEquals(new Layout(1, Type.INTEGER, true, Long.BYTES), Layout.of(long.class));
		}

		@Test
		void integers() {
			assertEquals(new Layout(1, Type.INTEGER, true, Integer.BYTES), Layout.of(Integer.class));
			assertEquals(new Layout(1, Type.INTEGER, true, Integer.BYTES), Layout.of(int.class));
		}

		@Test
		void shorts() {
			assertEquals(new Layout(1, Type.INTEGER, true, Short.BYTES), Layout.of(Short.class));
			assertEquals(new Layout(1, Type.INTEGER, true, Short.BYTES), Layout.of(short.class));
		}

		@Test
		void bytes() {
			assertEquals(new Layout(1, Type.INTEGER, true, 1), Layout.of(Byte.class));
			assertEquals(new Layout(1, Type.INTEGER, true, 1), Layout.of(byte.class));
		}

		@Test
		void booleans() {
			assertEquals(new Layout(1, Type.INTEGER, false, 1), Layout.of(Boolean.class));
			assertEquals(new Layout(1, Type.INTEGER, false, 1), Layout.of(boolean.class));
		}

		@Test
		void unsupported() {
			assertThrows(IllegalArgumentException.class, () -> Layout.of(String.class));
		}
	}

	@Test
	void equals() {
		assertEquals(component, component);
		assertEquals(component, new Layout(3, Type.FLOAT, false, Float.BYTES));
		assertNotEquals(component, null);
		assertNotEquals(component, new Layout(3, Type.NORMALIZED, true, 1));
	}
}
