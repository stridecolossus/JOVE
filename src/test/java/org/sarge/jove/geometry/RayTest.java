package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.*;

class RayTest {
	private Ray ray;
	private Intersection intersection;

	@BeforeEach
	void before() {
		ray = new Ray(Point.ORIGIN, Axis.X);
		intersection = new Intersection(new Point(1, 0, 0), 1, Axis.Y);
	}

	@Test
	void intersection() {
		assertEquals(intersection, ray.intersection(1, Axis.Y));
	}

	@Test
	void centre() {
		assertEquals(intersection, ray.intersection(1, new Point(1, -1, 0)));
	}

	@Test
	void none() {
		final var results = IntersectedSurface.EMPTY_INTERSECTIONS.iterator();
		assertEquals(false, results.hasNext());
	}

	@Test
	void compare() {
		assertEquals(0, intersection.compareTo(intersection));
		assertEquals(1, intersection.compareTo(new Intersection(Point.ORIGIN, 0, Axis.Y)));
	}
}
