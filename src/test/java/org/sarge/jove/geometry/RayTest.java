package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.Intersection;

class RayTest {
	private Ray ray;

	@BeforeEach
	void before() {
		ray = new Ray(Point.ORIGIN, Vector.X);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, ray.origin());
		assertEquals(Vector.X, ray.direction());
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
			assertArrayEquals(new float[0], Intersection.NONE.distances());
		}

		@Test
		void of() {
			final Intersection intersection = Intersection.of(1, 2);
			assertNotNull(intersection);
			assertArrayEquals(new float[]{1, 2}, intersection.distances());
		}
	}
}
