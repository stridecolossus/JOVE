package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.MathsUtil.*;

import org.junit.jupiter.api.*;

class SphereTest {
	private Sphere sphere;

	@BeforeEach
	void before() {
		sphere = new Sphere(Point.ORIGIN, 1);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, sphere.centre());
		assertEquals(1, sphere.radius());
	}

	@Nested
	class UnitSphereTests {
		@Test
		void top() {
			assertEquals(new Vector(0, -1, 0), Sphere.vector(0, -HALF_PI));
			assertEquals(new Vector(0, -1, 0), Sphere.vector(TWO_PI, -HALF_PI));
		}

		@Test
		void bottom() {
			assertEquals(new Vector(0, 1, 0), Sphere.vector(0, +HALF_PI));
			assertEquals(new Vector(0, 1, 0), Sphere.vector(TWO_PI, +HALF_PI));
		}

		@Test
		void middle() {
			assertEquals(new Vector(0, 0, 1), Sphere.vector(PI, 0));
		}
	}

	@Test
	void equals() {
		assertEquals(sphere, sphere);
		assertEquals(sphere, new Sphere(Point.ORIGIN, 1));
		assertNotEquals(sphere, null);
		assertNotEquals(sphere, new Sphere(Point.ORIGIN, 2));
	}
}
