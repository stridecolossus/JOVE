package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.*;

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
		@DisplayName("A bounding box intersects a sphere that it encloses")
		@Test
		void intersects() {
			assertEquals(true, box.intersects(new SphereVolume(bounds.centre(), 1)));
			assertEquals(true, box.intersects(new SphereVolume(bounds.centre(), 4)));
		}

		@DisplayName("A bounding box is intersected by a sphere that encloses its bounds")
		@Test
		void enclosed() {
			assertEquals(true, box.intersects(new SphereVolume(Point.ORIGIN, Float.MAX_VALUE)));
		}

		@DisplayName("A bounding box intersects a sphere that is touching the box")
		@Test
		void touching() {
			assertEquals(true, box.intersects(new SphereVolume(new Point(1, 2, 2), 1)));
		}

		@DisplayName("A bounding box does not intersect a sphere that is outside of its bounds")
		@Test
		void outside() {
			assertEquals(false, box.intersects(new SphereVolume(Point.ORIGIN, 3)));
		}
	}

	@Nested
	class BoxBoxIntersectionTests {
		@DisplayName("A bounding box intersects itself")
		@Test
		void self() {
			assertEquals(true, box.intersects(box));
		}

		@DisplayName("A bounding box intersects another box that is encloses")
		@Test
		void contains() {
			assertEquals(true, box.intersects(new BoundingBox(new Bounds(bounds.centre(), bounds.centre()))));
		}

		@DisplayName("A bounding box intersects another overl")
		@Test
		void overlapping() {
			assertEquals(true, box.intersects(new BoundingBox(new Bounds(Point.ORIGIN, bounds.min()))));
		}

		@DisplayName("A bounding box does not intersect another box that is outside of its bounds")
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
			ray = new DefaultRay(new Point(1, 2, 0), new Vector(2, 1, 0));
		}

		@DisplayName("A ray crossing a bounding box has two intersections")
		@Test
		void intersect() {
			final Iterator<Intersection> itr = box.intersect(ray);
			assertEquals(new Intersection(1, null), itr.next());
			assertEquals(new Intersection(2, null), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@DisplayName("A ray can intersect a corner of a bounding box")
		void corner() {
			final Iterator<Intersection> itr = box.intersect(new DefaultRay(new Point(3, 1, 0), ray.direction()));
			assertEquals(new Intersection(2, null), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@DisplayName("A ray that points away from the box has no intersections")
		@Test
		void miss() {
			assertEquals(Intersection.NONE, box.intersect(new DefaultRay(new Point(1, 4, 0), ray.direction())));
			assertEquals(Intersection.NONE, box.intersect(new DefaultRay(ray.origin(), new Vector(2, -1, 0))));
		}

		@DisplayName("A ray does not intersect a bounding box if it is behind the ray origin")
		@Test
		void behind() {
			assertEquals(Intersection.NONE, box.intersect(new DefaultRay(new Point(6, 3, 0), ray.direction())));
		}

		@DisplayName("A ray has a single intersects with a bounding box that it touches")
		@Test
		void parallel() {
			final Iterator<Intersection> itr = box.intersect(new DefaultRay(new Point(2, 2, 0), Vector.X));
			assertEquals(new Intersection(1, null), itr.next());
			assertEquals(new Intersection(3, null), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@DisplayName("A ray does not intersect a bounding box if the direction is parallel to that box")
		@Test
		void parallelMiss() {
			assertEquals(Intersection.NONE, box.intersect(new DefaultRay(new Point(1, 1, 0), Vector.X)));
		}
	}
}
