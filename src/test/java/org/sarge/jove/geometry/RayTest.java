package org.sarge.jove.geometry;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.geometry.Ray.Intersection.DefaultIntersection;

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

	@Nested
	class IntersectionTests {
		@Test
		void none() {
			assertEquals(0, Intersection.NONE.count());
		}

		@Test
		void intersection() {
			final Intersection intersection = new DefaultIntersection(Point.ORIGIN, 42);
			assertEquals(Point.ORIGIN, intersection.point());
			assertEquals(42f, intersection.distance());
		}

		@Test
		void stream() {
			final Stream<Intersection> intersections = Intersection.stream(Point.ORIGIN, 42);
			assertNotNull(intersections);
			assertEquals(List.of(new DefaultIntersection(Point.ORIGIN, 42)), intersections.collect(toList()));
		}
	}
}
