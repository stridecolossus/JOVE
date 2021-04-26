package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;

class BoundingBoxTest {
	private BoundingBox box;
	private Extents extents;

	@BeforeEach
	void before() {
		extents = new Extents(new Point(1, 2, 3), new Point(4, 5, 6));
		box = new BoundingBox(extents);
	}

	@Test
	void constructor() {
		assertEquals(extents, box.extents());
	}

	@Test
	void contains() {
		assertTrue(box.contains(new Point(1, 2, 3)));
		assertTrue(box.contains(new Point(2, 3, 4)));
		assertTrue(box.contains(new Point(4, 5, 6)));
		assertFalse(box.contains(Point.ORIGIN));
	}

	@Test
	void intersects() {
		final Volume vol = mock(Volume.class);
		when(vol.extents()).thenReturn(extents);
		assertEquals(true, box.intersects(vol));
	}

	@Nested
	class BoxSphereIntersectionTests {
		private Point centre;

		@BeforeEach
		void before() {
			centre = extents.centre();
		}

		@Test
		void intersects() {
			assertEquals(true, box.intersects(new SphereVolume(centre, 1)));
			assertEquals(true, box.intersects(new SphereVolume(centre, 4)));
			assertEquals(true, box.intersects(new SphereVolume(centre, Float.MAX_VALUE)));
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
			assertEquals(true, box.intersects(new BoundingBox(extents)));
		}

		@Test
		void overlapping() {
			assertEquals(true, box.intersects(new BoundingBox(new Extents(Point.ORIGIN, extents.min()))));
		}

		@Test
		void outside() {
			assertEquals(false, box.intersects(new BoundingBox(new Extents(Point.ORIGIN, Point.ORIGIN))));
		}
	}

	@Test
	void intersectRay() {
		// TODO
	}
}
