package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RectangleTest {
	private Rectangle rect;

	@BeforeEach
	void before() {
		rect = new Rectangle(1, 2, 3, 4);
	}

	@Test
	void constructor() {
		assertEquals(1, rect.x());
		assertEquals(2, rect.y());
		assertEquals(3, rect.width());
		assertEquals(4, rect.height());
	}

	@Test
	void constructorDimensions() {
		assertEquals(rect, new Rectangle(1, 2, new Dimensions(3, 4)));
	}

	@Test
	void dimensions() {
		assertEquals(new Dimensions(3, 4), rect.dimensions());
	}

	@Test
	void equals() {
		assertEquals(true, rect.equals(rect));
		assertEquals(true, rect.equals(new Rectangle(1, 2, 3, 4)));
		assertEquals(false, rect.equals(null));
		assertEquals(false, rect.equals(new Rectangle(1, 2, 8, 9)));
	}
}
