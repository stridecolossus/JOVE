package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.geometry.Vector.*;
import static org.sarge.jove.util.MathsUtil.*;

import java.util.Iterator;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.*;
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
	@DisplayName("Create a sphere containing the given bounds")
	void of() {
		sphere = SphereVolume.of(new Bounds(new Point(1, 2, 3), new Point(5, 6, 7)));
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

	@DisplayName("Intersection tests delegate to a sphere intersection")
	@Test
	void intersectDelegate() {
		final Volume vol = mock(Volume.class);
		assertEquals(false, sphere.intersects(vol));
		verify(vol).intersects(sphere);
	}

	@Nested
	class SpherePlaneIntersectionTests {
		@Test
		void axes() {
			assertEquals(true, sphere.intersects(new Plane(X, 0)));
			assertEquals(true, sphere.intersects(new Plane(Y, 0)));
			assertEquals(true, sphere.intersects(new Plane(Z, 0)));
		}

		@Test
		void inside() {
			assertEquals(true, sphere.intersects(new Plane(X, 1)));
			assertEquals(true, sphere.intersects(new Plane(X, -1)));
		}

		@Test
		void touching() {
			assertEquals(true, sphere.intersects(new Plane(X, RADIUS)));
			assertEquals(true, sphere.intersects(new Plane(X, -RADIUS)));
		}

		@Test
		void outside() {
			assertEquals(false, sphere.intersects(new Plane(X, OUTSIDE)));
			assertEquals(false, sphere.intersects(new Plane(X, -OUTSIDE)));
		}
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
			assertEquals(true, sphere.intersects(new Bounds(Point.ORIGIN, Point.ORIGIN)));
		}

		@DisplayName("Oberlapping extents and sphere intersects")
		@Test
		void intersects() {
			assertEquals(true, sphere.intersects(new Bounds(Point.ORIGIN, new Point(RADIUS, 0, 0))));
		}

		@DisplayName("Sphere touching a corner intersects")
		@Test
		void touching() {
			final Point pt = new Point(RADIUS, 0, 0);
			assertEquals(true, sphere.intersects(new Bounds(pt, pt)));
		}

		@DisplayName("Sphere outside the extents does not intersect")
		@Test
		void outside() {
			final Point pt = new Point(OUTSIDE, 0, 0);
			assertEquals(false, sphere.intersects(new Bounds(pt, pt)));
		}
	}

	@Nested
	class SphereRayIntersectionTests {
		@DisplayName("Sphere behind, does not intersect")
		@Test
		void behindNotIntersecting() {
			assertEquals(Intersected.NONE, sphere.intersections(new DefaultRay(new Point(OUTSIDE, 0, 0), X)));
		}

		@DisplayName("Sphere behind, ray originates inside the sphere")
		@Test
		void behindInside() {
			final Ray ray = new DefaultRay(new Point(1, 0, 0), X);
			final Iterator<Intersection> itr = sphere.intersections(ray);
			assertEquals(new Intersection(ray, 2, X), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@DisplayName("Sphere behind, ray originates on the sphere surface")
		@Test
		void behindTouching() {
			final Ray ray = new DefaultRay(new Point(RADIUS, 0, 0), X);
			final Iterator<Intersection> itr = sphere.intersections(ray);
			assertEquals(new Intersection(ray, 0, X), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@DisplayName("Sphere ahead, does not intersect")
		@Test
		void outside() {
			assertEquals(Intersected.NONE, sphere.intersections(new DefaultRay(new Point(-OUTSIDE, +OUTSIDE, 0), X)));
		}

		@DisplayName("Sphere ahead, ray originates inside the sphere")
		@Test
		void inside() {
			final Ray ray = new DefaultRay(new Point(-1, 0, 0), X);
			final Iterator<Intersection> itr = sphere.intersections(ray);
			assertEquals(new Intersection(ray, 4, X), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@DisplayName("Sphere ahead, intersects twice")
		@Test
		void intersects() {
			final Ray ray = new DefaultRay(new Point(-OUTSIDE, 0, 0), X);
			final Iterator<Intersection> itr = sphere.intersections(ray);
			assertEquals(new Intersection(ray, 1, X.invert()), itr.next());
			assertEquals(new Intersection(ray, 7, X), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@DisplayName("Sphere ahead, ray originates on the sphere surface")
		@Test
		void touching() {
			final Ray ray = new DefaultRay(new Point(-RADIUS, 0, 0), X);
			final Iterator<Intersection> itr = sphere.intersections(ray);
			assertEquals(new Intersection(ray, 0, X.invert()), itr.next());
			assertEquals(new Intersection(ray, 6, X), itr.next());
			assertEquals(false, itr.hasNext());
		}
	}

	@Nested
	class UnitSphereTests {
		@Test
		void top() {
			assertEquals(new Vector(0, -1, 0), SphereVolume.vector(0, -HALF_PI));
			assertEquals(new Vector(0, -1, 0), SphereVolume.vector(TWO_PI, -HALF_PI));
		}

		@Test
		void bottom() {
			assertEquals(new Vector(0, 1, 0), SphereVolume.vector(0, +HALF_PI));
			assertEquals(new Vector(0, 1, 0), SphereVolume.vector(TWO_PI, +HALF_PI));
		}

		@Test
		void middle() {
			assertEquals(new Vector(0, 0, 1), SphereVolume.vector(PI, 0));
		}
	}

	@Test
	void equals() {
		assertEquals(sphere, sphere);
		assertEquals(sphere, new SphereVolume(Point.ORIGIN, RADIUS));
		assertNotEquals(sphere, null);
		assertNotEquals(sphere, new SphereVolume(Point.ORIGIN, 42));
	}
}
