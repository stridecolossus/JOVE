package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;

class CompoundLayoutTest {
	private CompoundLayout layout;
	private Layout component;

	@BeforeEach
	void before() {
		component = Layout.floats(3);
		layout = new CompoundLayout(component, component);
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
	void contains() {
		assertEquals(true, layout.contains(component));
		assertEquals(false, layout.contains(null));
	}

	@Test
	void equals() {
		assertEquals(layout, layout);
		assertEquals(layout, new CompoundLayout(component, component));
		assertNotEquals(layout, null);
		assertNotEquals(layout, new CompoundLayout());
	}
}
