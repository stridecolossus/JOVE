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
		assertEquals(3, component.size());
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

	@Test
	void equals() {
		assertEquals(component, component);
		assertEquals(component, new Component(3, Type.FLOAT, false, Float.BYTES));
		assertNotEquals(component, null);
		assertNotEquals(component, new Component(3, Type.NORMALIZED, true, 1));
	}
}
