package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PointTest {
	@Test
	public void origin() {
		assertEquals(new Point(0, 0, 0), Point.ORIGIN);
	}

	@Test
	public void distance() {
		final Point start = new Point(1, 2, 3);
		final Point end = new Point(4, 5, 6);
		assertEquals(27f, start.distance(end), 0.0001f);
	}

	@Test
	public void converter() {
		final Point result = Point.CONVERTER.apply("1,2,3");
		assertEquals(new Point(1, 2, 3), result);
	}
}
