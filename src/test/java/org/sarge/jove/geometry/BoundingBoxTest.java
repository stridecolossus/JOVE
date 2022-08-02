package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.Intersection;

class BoundingBoxTest {
	private BoundingBox box;
	private Bounds bounds;

	@BeforeEach
	void before() {
		bounds = new Bounds(new Point(1, 2, 3), new Point(4, 5, 6));
		box = new BoundingBox(bounds);
	}

	@DisplayName("A bounding box is an adapter for min/max bounds")
	@Test
	void constructor() {
		assertEquals(bounds, box.bounds());
	}

	@DisplayName("A bounding box can determine whether a given point lies within the bounds")
	@Test
	void contains() {
		assertTrue(box.contains(new Point(1, 2, 3)));
		assertTrue(box.contains(new Point(2, 3, 4)));
		assertTrue(box.contains(new Point(4, 5, 6)));
		assertFalse(box.contains(Point.ORIGIN));
	}

	@Test
	void intersects() {
		assertEquals(false, box.intersects(Volume.EMPTY));
	}

	@Nested
	class BoxSphereIntersectionTests {
		@Test
		void intersects() {
			assertEquals(true, box.intersects(new SphereVolume(bounds.centre(), 1)));
			assertEquals(true, box.intersects(new SphereVolume(bounds.centre(), 4)));
			assertEquals(true, box.intersects(new SphereVolume(bounds.centre(), Float.MAX_VALUE)));
		}

		@Test
		void touching() {
			assertEquals(true, box.intersects(new SphereVolume(new Point(1, 2, 2), 1)));
		}

		@Test
		void outside() {
			assertEquals(false, box.intersects(new SphereVolume(Point.ORIGIN, 3)));
		}
	}

	@Nested
	class BoxBoxIntersectionTests {
		@Test
		void self() {
			assertEquals(true, box.intersects(box));
			assertEquals(true, box.intersects(new BoundingBox(bounds)));
		}

		@Test
		void overlapping() {
			assertEquals(true, box.intersects(new BoundingBox(new Bounds(Point.ORIGIN, bounds.min()))));
		}

		@Test
		void outside() {
			assertEquals(false, box.intersects(new BoundingBox(new Bounds(Point.ORIGIN, Point.ORIGIN))));
		}
	}

	@Nested
	class BoxRayIntersectionTests {
		private Ray ray;

		@BeforeEach
		void before() {
			bounds = new Bounds(new Point(3, 2, 0), new Point(5, 4, 0));
			box = new BoundingBox(bounds);
			ray = new Ray(new Point(1, 2, 0), new Vector(2, 1, 0));
		}

		@DisplayName("Ray intersects the box twice")
		@Test
		void intersect() {
			final Intersection intersection = box.intersect(ray);
			assertNotNull(intersection);
			assertEquals(List.of(1f, 2f), intersection.distances());
		}

		@DisplayName("Ray intersects a corner of the box")
		void corner() {
			final Intersection intersection = box.intersect(new Ray(new Point(3, 1, 0), ray.direction()));
			assertNotNull(intersection);
			assertEquals(List.of(2f), intersection.distances());
		}

		@DisplayName("Ray does not intersect the box")
		@Test
		void miss() {
			assertEquals(Intersection.NONE, box.intersect(new Ray(new Point(1, 4, 0), ray.direction())));
			assertEquals(Intersection.NONE, box.intersect(new Ray(ray.origin(), new Vector(2, -1, 0))));
		}

		@DisplayName("Box is behind the ray origin")
		@Test
		void behind() {
			assertEquals(Intersection.NONE, box.intersect(new Ray(new Point(6, 3, 0), ray.direction())));
		}

		@DisplayName("Direction is parallel to the box")
		@Test
		void parallel() {
			final Intersection intersection = box.intersect(new Ray(new Point(2, 2, 0), Vector.X));
			assertNotNull(intersection);
			assertEquals(List.of(1f, 3f), intersection.distances());
		}

		@DisplayName("Direction is parallel but does not intersect")
		@Test
		void parallelMiss() {
			assertEquals(Intersection.NONE, box.intersect(new Ray(new Point(1, 1, 0), Vector.X)));
		}
	}
}
