package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.*;

class RayTest {
	private Ray ray;

	@BeforeEach
	void before() {
		ray = new DefaultRay(Point.ORIGIN, Axis.X);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, ray.origin());
		assertEquals(Axis.X, ray.direction());
	}

	@Test
	void point() {
		assertEquals(Point.ORIGIN, ray.point(0));
		assertEquals(new Point(1, 0, 0), ray.point(1));
	}

	@Test
	void equals() {
		assertEquals(ray, ray);
		assertEquals(ray, new DefaultRay(Point.ORIGIN, Axis.X));
		assertNotEquals(ray, null);
		assertNotEquals(ray, mock(Ray.class));
	}

	@Nested
	class IntersectionTests {
		@Test
		void none() {
			assertEquals(true, Intersected.NONE.isEmpty());
			assertArrayEquals(new float[0], Intersected.NONE.distances());
			assertThrows(UnsupportedOperationException.class, () -> Intersected.NONE.normal(null));
		}

		@Test
		void of() {
			final var intersection = Intersection.of(3, Axis.Y);
			assertEquals(false, intersection.isEmpty());
			assertArrayEquals(new float[]{3}, intersection.distances());
			assertEquals(Axis.Y, intersection.normal(null));
		}
	}
}
