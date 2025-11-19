package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Point.ORIGIN;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.*;

class RayTest {
	private Ray ray;

	@BeforeEach
	void before() {
		ray = new Ray(ORIGIN, Axis.X);
	}

	@Test
	void point() {
		assertEquals(ORIGIN, ray.point(0));
		assertEquals(new Point(Axis.X.multiply(3)), ray.point(3));
	}

	@Test
	void normal() {
		assertEquals(Axis.X.invert(), IntersectedSurface.normal(ORIGIN, new Point(Axis.X)));
	}

	@Test
	void order() {
		final var surface = new Plane(Axis.X, 0);
		final var one = new Intersection(1, surface);
		final var two = new Intersection(2, surface);
		assertEquals(-1, one.compareTo(two));
		assertEquals(+1, two.compareTo(one));
		assertEquals( 0, one.compareTo(one));
	}
}
