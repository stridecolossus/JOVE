package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlaneTest {
	private Plane plane;

	@BeforeEach
	void before() {
		plane = new Plane(Vector.X_AXIS, 3);
	}

	@Test
	void constructor() {
		assertEquals(Vector.X_AXIS, plane.normal());
		assertEquals(3, plane.distance());
	}

	@Test
	void distance() {
		assertEquals(-3f, plane.distance(Point.ORIGIN));
		assertEquals(0f, plane.distance(new Point(3, 0, 0)));
	}

	@Test
	void side() {
		assertEquals(Plane.Side.FRONT, plane.side(new Point(4, 0, 0)));
		assertEquals(Plane.Side.BACK, plane.side(Point.ORIGIN));
		assertEquals(Plane.Side.INTERSECT, plane.side(new Point(3, 0, 0)));
	}

	@Test
	void triangle() {
		final Plane result = Plane.of(new Point(-3, 0, 0), new Point(-3, 1, 0), new Point(-3, 0, 1));
		assertEquals(plane, result);
	}

	@Test
	void of() {
		final Plane result = Plane.of(Vector.X_AXIS, new Point(-3, 0, 0));
		assertEquals(plane, result);
	}
}
