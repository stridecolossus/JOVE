package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.List;

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
			assertEquals(true, Intersected.NONE.isEmpty());
			assertEquals(List.of(), Intersected.NONE.distances());
			assertThrows(UnsupportedOperationException.class, () -> Intersected.NONE.normal(null));
		}

		@Test
		void of() {
			final var intersection = Intersection.of(3, Vector.Y);
			assertEquals(false, intersection.isEmpty());
			assertEquals(List.of(3f), intersection.distances());
			assertEquals(new Point(3, 0, 0), intersection.point(ray));
			assertEquals(Vector.Y, intersection.normal(null));
		}
	}
}
