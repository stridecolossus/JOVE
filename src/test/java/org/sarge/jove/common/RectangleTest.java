package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RectangleTest {
	private Rectangle rect;

	@BeforeEach
	public void before() {
		rect = new Rectangle(new ScreenCoordinate(1, 2), new Dimensions(3, 4));
	}

	@Test
	public void constructor() {
		assertEquals(new ScreenCoordinate(1, 2), rect.position());
		assertEquals(new Dimensions(3, 4), rect.dimensions());
	}

	@Test
	public void equals() {
		assertEquals(true, rect.equals(rect));
		assertEquals(false, rect.equals(null));
		assertEquals(false, rect.equals(new Rectangle(new ScreenCoordinate(1, 2), new Dimensions(8, 9))));
	}
}
