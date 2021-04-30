package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Ray.Intersection;

class RayTest {
	private Ray ray;

	@BeforeEach
	void before() {
		ray = new Ray(Point.ORIGIN, Vector.X_AXIS);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, ray.origin());
		assertEquals(Vector.X_AXIS, ray.direction());
	}

	@Test
	void scale() {
		assertEquals(Point.ORIGIN, ray.point(0));
		assertEquals(new Point(1, 0, 0), ray.point(1));
		assertEquals(new Point(2, 0, 0), ray.point(2));
	}

	@Nested
	class IntersectionTests {
		@Test
		void none() {
			assertEquals(List.of(), Intersection.NONE.distances());
			assertEquals(true, Intersection.NONE.equals(Intersection.NONE));
		}

		@Test
		void of() {
			final float result = 42;
			final Intersection intersection = Intersection.of(result);
			assertNotNull(intersection);
			assertEquals(List.of(result), intersection.distances());
		}
	}
}
