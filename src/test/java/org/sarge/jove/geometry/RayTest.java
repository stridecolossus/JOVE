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

		intersection = ray.new AbstractIntersection(1) {
			@Override
			public Point point() {
				return new Point(1, 0, 0);
			}

			@Override
			public Normal normal() {
				return Axis.Y;
			}
		};
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
		assertEquals(1, intersection.compareTo(ray.intersection(0, Axis.Y)));
	}
}
