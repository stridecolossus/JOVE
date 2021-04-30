package org.sarge.jove.scene;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

class SphereVolumeTest {
	private static final float RADIUS = 3;
	private static final float OUTSIDE = 4;

	private SphereVolume sphere;

	@BeforeEach
	void before() {
		sphere = new SphereVolume(Point.ORIGIN, RADIUS);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, sphere.centre());
		assertEquals(3f, sphere.radius());
	}

	@Test
	void extents() {
		assertEquals(new Extents(new Point(-RADIUS, -RADIUS, -RADIUS), new Point(RADIUS, RADIUS, RADIUS)), sphere.extents());
	}

	@Test
	@DisplayName("Create a sphere containing the given extents")
	void of() {
		sphere = SphereVolume.of(new Extents(new Point(1, 2, 3), new Point(5, 6, 7)));
		assertNotNull(sphere);
		assertEquals(new Point(3, 4, 5), sphere.centre());
		assertEquals(2f, sphere.radius());
	}

	@Test
	void contains() {
		assertEquals(true, sphere.contains(Point.ORIGIN));
		assertEquals(true, sphere.contains(new Point(RADIUS, 0, 0)));
		assertEquals(false, sphere.contains(new Point(OUTSIDE, 0, 0)));
	}

	@Test
	void within() {
		assertEquals(true, sphere.within(0, RADIUS));
		assertEquals(true, sphere.within(RADIUS * RADIUS, RADIUS));
		assertEquals(false, sphere.within(OUTSIDE * OUTSIDE, RADIUS));
	}

	@Test
	void intersects() {
		final Volume vol = mock(Volume.class);
		when(vol.extents()).thenReturn(new Extents(Point.ORIGIN, Point.ORIGIN));
		assertEquals(true, sphere.intersects(vol));
	}

	@Nested
	class SphereSphereIntersectionTests {
		private Point outside;

		@BeforeEach
		void before() {
			outside = new Point(OUTSIDE, 0, 0);
		}

		@DisplayName("Sphere should always intersect itself")
		@Test
		void self() {
			assertEquals(true, sphere.intersects(sphere));
		}

		@DisplayName("Contained sphere intersects")
		@Test
		void inside() {
			assertEquals(true, sphere.intersects(new SphereVolume(Point.ORIGIN, 1)));
		}

		@DisplayName("Overlapping spheres should intersect")
		@Test
		void intersecting() {
			assertEquals(true, sphere.intersects(new SphereVolume(outside, 2)));
		}

		@DisplayName("Touching spheres intersect")
		@Test
		void touching() {
			assertEquals(true, sphere.intersects(new SphereVolume(outside, 1)));
		}

		@DisplayName("Non-intersecting spheres")
		@Test
		void outside() {
			assertEquals(false, sphere.intersects(new SphereVolume(outside, MathsUtil.HALF)));
		}
	}

	@Nested
	class SphereExtentsIntersectionTests {
		@DisplayName("Extents contained by sphere intersects")
		@Test
		void inside() {
			assertEquals(true, sphere.intersects(new Extents(Point.ORIGIN, Point.ORIGIN)));
		}

		@DisplayName("Oberlapping extents and sphere intersects")
		@Test
		void intersects() {
			assertEquals(true, sphere.intersects(new Extents(Point.ORIGIN, new Point(RADIUS, 0, 0))));
		}

		@DisplayName("Sphere touching a corner intersects")
		@Test
		void touching() {
			final Point pt = new Point(RADIUS, 0, 0);
			assertEquals(true, sphere.intersects(new Extents(pt, pt)));
		}

		@DisplayName("Sphere outside the extents does not intersect")
		@Test
		void outside() {
			final Point pt = new Point(OUTSIDE, 0, 0);
			assertEquals(false, sphere.intersects(new Extents(pt, pt)));
		}
	}

	@Nested
	class SphereRayIntersectionTests {
		/**
		 * @param origin			Ray origin
		 * @param expected			Expected intersection points (X axis only, relative to ray origin)
		 */
		private void test(Point origin, Float... expected) {
			// Calc expected distances relative to ray origin
			final var list = Arrays.stream(expected).map(n -> n - origin.x).collect(toList());

			// Check intersection results
			final Intersection result = sphere.intersect(new Ray(origin, Vector.X_AXIS));
			assertNotNull(result);
			assertEquals(list, result.distances());
		}

		@DisplayName("Sphere behind, does not intersect")
		@Test
		void behindNotIntersecting() {
			test(new Point(OUTSIDE, 0, 0));
		}

		@DisplayName("Sphere behind, ray originates inside the sphere")
		@Test
		void behindInside() {
			test(new Point(1, 0, 0), RADIUS);
		}

		@DisplayName("Sphere behind, ray originates on the sphere surface")
		@Test
		void behindTouching() {
			test(new Point(RADIUS, 0, 0), RADIUS);
		}

		@DisplayName("Sphere ahead, does not intersect")
		@Test
		void outside() {
			test(new Point(-OUTSIDE, OUTSIDE, 0));
		}

		@DisplayName("Sphere ahead, ray originates inside the sphere")
		@Test
		void inside() {
			test(new Point(-1, 0, 0), RADIUS);
		}

		@DisplayName("Sphere ahead, intersects twice")
		@Test
		void intersects() {
			// TODO
			test(new Point(-OUTSIDE, 0, 0), -RADIUS, RADIUS);
		}

		@DisplayName("Sphere ahead, ray originates on the sphere surface")
		@Test
		void touching() {
			test(new Point(-RADIUS, 0, 0), -RADIUS, RADIUS);
		}
	}

	@Test
	void equals() {
		assertEquals(true, sphere.equals(sphere));
		assertEquals(true, sphere.equals(new SphereVolume(Point.ORIGIN, RADIUS)));
		assertEquals(false, sphere.equals(null));
		assertEquals(false, sphere.equals(new SphereVolume(Point.ORIGIN, 42)));
	}
}
