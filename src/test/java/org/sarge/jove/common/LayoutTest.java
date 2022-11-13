package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;

class LayoutTest {
	private Layout layout;
	private Component component;

	@BeforeEach
	void before() {
		component = Component.floats(3);
		layout = new Layout(component, component);
	}

	@Test
	void components() {
		assertEquals(List.of(component, component), layout.components());
	}

	@Test
	void stride() {
		assertEquals(2 * 3 * Float.BYTES, layout.stride());
	}

	@Test
	void length() {
		assertEquals(2 * 3 * Float.BYTES, layout.stride());
	}

	@Test
	void equals() {
		assertEquals(layout, layout);
		assertEquals(layout, new Layout(component, component));
		assertNotEquals(layout, null);
		assertNotEquals(layout, new Layout());
	}
}
