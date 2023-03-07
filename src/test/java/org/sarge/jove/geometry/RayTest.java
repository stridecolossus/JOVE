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
	void equals() {
		assertEquals(ray, ray);
		assertEquals(ray, new DefaultRay(Point.ORIGIN, Axis.X));
		assertNotEquals(ray, null);
		assertNotEquals(ray, mock(Ray.class));
	}

	@DisplayName("A simple intersection result can be constructed with a single intersection and surface normal")
	@Test
	void of() {
		final Intersection result = Intersection.of(ray, 1, Axis.Y);
		assertEquals(1f, result.distance());
		assertEquals(new Point(Axis.X), result.point());
		assertEquals(Axis.Y, result.normal());
	}

	@DisplayName("An intersection result can be constructed with a surface normal relative to the centre of the intresected volume")
	@Test
	void centre() {
		final Intersection result = Intersection.of(ray, 1, Point.ORIGIN);
		assertEquals(1f, result.distance());
		assertEquals(new Point(Axis.X), result.point());
		assertEquals(Axis.X, result.normal());
	}

	@DisplayName("An empty intersection result has no values")
	@Test
	void none() {
		final var results = Intersected.NONE.iterator();
		assertEquals(false, results.hasNext());
	}

	@DisplayName("An intersection...")
	@Nested
	class DefaultIntersectionTests {
		private DefaultIntersection intersection;

		@BeforeEach
		void before() {
			intersection = new DefaultIntersection(ray, 1);
		}

		@DisplayName("has an intersection point on the given ray")
		@Test
		void constructor() {
			assertEquals(1f, intersection.distance());
			assertEquals(new Point(Axis.X), intersection.point());
		}

		@DisplayName("can be ordered by distance")
		@Test
		void compare() {
			assertEquals(0, Intersection.COMPARATOR.compare(intersection, intersection));
			assertEquals(1, Intersection.COMPARATOR.compare(intersection, mock(Intersection.class)));
		}

		@Test
		void equals() {
			assertEquals(intersection, intersection);
			assertNotEquals(intersection, null);
			assertNotEquals(intersection, mock(Intersection.class));
		}
	}
}
