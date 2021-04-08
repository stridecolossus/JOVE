package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlaneTest {
	private Plane plane;

	@BeforeEach
	public void before() {
		plane = new Plane(Vector.X_AXIS, 3);
	}

	@Test
	public void constructor() {
		assertEquals(Vector.X_AXIS, plane.normal());
		assertEquals(3, plane.distance());
	}

	@Test
	public void distance() {
		assertEquals(-3, plane.distanceTo(Point.ORIGIN));
		// TODO - what is distance used for anyway?
		//assertEquals(-5, plane.distance(new Point(0, 4, 0)));
	}

	@Test
	public void side() {
		assertEquals(Plane.Side.FRONT, plane.side(new Point(4, 0, 0)));
		assertEquals(Plane.Side.BACK, plane.side(Point.ORIGIN));
		assertEquals(Plane.Side.INTERSECT, plane.side(new Point(3, 0, 0)));
	}

	@Test
	public void createFromTriangle() {
		final Plane result = Plane.of(new Point(-3, 0, 0), new Point(-3, 1, 0), new Point(-3, 0, 1));
		assertEquals(plane, result);
	}

	@Test
	public void createFromPointNormal() {
		final Plane result = Plane.of(Vector.X_AXIS, new Point(-3, 0, 0));
		assertEquals(plane, result);
	}
}
