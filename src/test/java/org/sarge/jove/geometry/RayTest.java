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

	@Nested
	class IntersectedSurfaceTests {
		@DisplayName("An empty intersection result cannot be iterated")
		@Test
		void none() {
			final var results = Intersected.NONE.iterator();
			assertEquals(false, results.hasNext());
		}

		@DisplayName("An undefined intersection result cannot be queried")
		@Test
		void undefined() {
			final var results = Intersected.UNDEFINED.iterator();
			assertEquals(true, results.hasNext());
			assertThrows(UnsupportedOperationException.class, () -> results.next());
		}
	}

	@DisplayName("A simple intersection result can be constructed with a single intersection and surface normal")
	@Test
	void of() {
		final Intersection result = Intersection.of(ray, 1, Axis.Y);
		assertEquals(1f, result.distance());
		assertEquals(new Point(Axis.X), result.point());
		assertEquals(Axis.Y, result.normal());
	}

	@DisplayName("An intersection...")
	@Nested
	class DefaultIntersectionTests {
		private AbstractIntersection intersection;

		@BeforeEach
		void before() {
			intersection = new DefaultIntersection(ray, 1, Point.ORIGIN);
		}

		@DisplayName("has an intersection point on the given ray")
		@Test
		void constructor() {
			assertEquals(1f, intersection.distance());
			assertEquals(new Point(Axis.X), intersection.point());
		}

		@DisplayName("can calculate the surface normal relative to a given centre point")
		@Test
		void normal() {
			assertEquals(Axis.X, intersection.normal());
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
