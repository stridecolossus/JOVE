package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.*;

class RayTest {
	private Ray ray;

	@BeforeEach
	void before() {
		ray = new DefaultRay(Point.ORIGIN, Vector.X);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, ray.origin());
		assertEquals(Vector.X, ray.direction());
	}

	@Test
	void point() {
		assertEquals(Point.ORIGIN, ray.point(0));
		assertEquals(new Point(1, 0, 0), ray.point(1));
	}

	@Test
	void equals() {
		assertEquals(ray, ray);
		assertEquals(ray, new DefaultRay(Point.ORIGIN, Vector.X));
		assertNotEquals(ray, null);
		assertNotEquals(ray, mock(Ray.class));
	}

	@Nested
	class IntersectionTests {
		@Test
		void none() {
			assertEquals(false, Intersects.NONE.hasNext());
		}

		@Test
		void intersection() {
			final var intersection = new Intersection(ray, 3, Vector.Y);
			assertEquals(new Point(3, 0, 0), intersection.point());
			assertEquals(Vector.Y, intersection.normal());
		}
	}
}
