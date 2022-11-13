package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Component.Type;

public class ComponentTest {
	private Component component;

	@BeforeEach
	void before() {
		component = new Component(3, Type.FLOAT, false, Float.BYTES);
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
		assertEquals(new Component(3, Type.FLOAT, true, Float.BYTES), Component.floats(3));
	}

	@Nested
	class TypeTests {
		@Test
		void floats() {
			assertEquals(new Component(1, Type.FLOAT, true, Float.BYTES), Component.of(Float.class));
			assertEquals(new Component(1, Type.FLOAT, true, Float.BYTES), Component.of(float.class));
		}

		@Test
		void longs() {
			assertEquals(new Component(1, Type.INTEGER, true, Long.BYTES), Component.of(Long.class));
			assertEquals(new Component(1, Type.INTEGER, true, Long.BYTES), Component.of(long.class));
		}

		@Test
		void integers() {
			assertEquals(new Component(1, Type.INTEGER, true, Integer.BYTES), Component.of(Integer.class));
			assertEquals(new Component(1, Type.INTEGER, true, Integer.BYTES), Component.of(int.class));
		}

		@Test
		void shorts() {
			assertEquals(new Component(1, Type.INTEGER, true, Short.BYTES), Component.of(Short.class));
			assertEquals(new Component(1, Type.INTEGER, true, Short.BYTES), Component.of(short.class));
		}

		@Test
		void bytes() {
			assertEquals(new Component(1, Type.INTEGER, true, 1), Component.of(Byte.class));
			assertEquals(new Component(1, Type.INTEGER, true, 1), Component.of(byte.class));
		}

		@Test
		void booleans() {
			assertEquals(new Component(1, Type.INTEGER, false, 1), Component.of(Boolean.class));
			assertEquals(new Component(1, Type.INTEGER, false, 1), Component.of(boolean.class));
		}

		@Test
		void unsupported() {
			assertThrows(IllegalArgumentException.class, () -> Component.of(String.class));
		}
	}

	@Test
	void equals() {
		assertEquals(component, component);
		assertEquals(component, new Component(3, Type.FLOAT, false, Float.BYTES));
		assertNotEquals(component, null);
		assertNotEquals(component, new Component(3, Type.NORMALIZED, true, 1));
	}
}
