package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Component.Layout;

class ComponentTest {
	private Layout layout;

	@BeforeEach
	void before() {
		layout = new Layout(3, 4, Float.class);
	}

	@Test
	void length() {
		final Component component = spy(Component.class);
		when(component.layout()).thenReturn(layout);
		assertEquals(3 * 4, component.length());
	}

	@Nested
	class LayoutTests {
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
			layout = new Layout(3, 4, Integer.class);
			assertEquals(layout, Layout.of(3, Integer.class));
			assertEquals(layout, Layout.of(3, Integer.TYPE));
		}

		@Test
		void shorts() {
			layout = new Layout(3, 2, Short.class);
			assertEquals(layout, Layout.of(3, Short.class));
			assertEquals(layout, Layout.of(3, Short.TYPE));
		}

		@Test
		void of() {
			assertEquals(layout, Layout.of(3));
		}
	}
}
