package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LayoutTest {
	private Layout layout;

	@BeforeEach
	void before() {
		layout = new Layout(3, 4, Float.class);
	}

	@Test
	void constructor() {
		assertEquals(3, layout.size());
		assertEquals(4, layout.bytes());
		assertEquals(Float.class, layout.type());
	}

	@Test
	void length() {
		assertEquals(3 * 4, layout.length());
	}

	@Test
	void floats() {
		assertEquals(layout, Layout.of(3, Float.class));
		assertEquals(layout, Layout.of(3, Float.TYPE));
	}

	@Test
	void integers() {
		final Layout ints = new Layout(3, 4, Integer.class);
		assertEquals(ints, Layout.of(3, Integer.class));
		assertEquals(ints, Layout.of(3, Integer.TYPE));
	}

	@Test
	void shorts() {
		final Layout shorts = new Layout(3, 2, Short.class);
		assertEquals(shorts, Layout.of(3, Short.class));
		assertEquals(shorts, Layout.of(3, Short.TYPE));
	}

	@Test
	void of() {
		assertEquals(layout, Layout.of(3));
	}
}
