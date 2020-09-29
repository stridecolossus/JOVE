package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RectangleTest {
	private Rectangle rect;

	@BeforeEach
	void before() {
		rect = new Rectangle(new Coordinate(1, 2), new Dimensions(3, 4));
	}

	@Test
	void constructor() {
		assertEquals(new Coordinate(1, 2), rect.pos());
		assertEquals(new Dimensions(3, 4), rect.size());
		assertEquals(1, rect.x());
		assertEquals(2, rect.y());
		assertEquals(3, rect.width());
		assertEquals(4, rect.height());
	}

	@Test
	void constructorDimensions() {
		rect = new Rectangle(new Dimensions(3, 4));
		assertEquals(Coordinate.ORIGIN, rect.pos());
		assertEquals(new Dimensions(3, 4), rect.size());
	}

	@Test
	void constructorComponents() {
		assertEquals(rect, new Rectangle(1, 2, 3, 4));
	}

	@Test
	void equals() {
		assertEquals(true, rect.equals(rect));
		assertEquals(false, rect.equals(null));
		assertEquals(false, rect.equals(new Rectangle(new Coordinate(1, 2), new Dimensions(8, 9))));
	}
}
