package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.MathsUtility.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Sphere.NormalFactory;

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
	class NormalFactoryTests {
		private NormalFactory factory;

		@BeforeEach
		void before() {
			factory = new NormalFactory().rotate();
		}

		@Test
		void top() {
			assertEquals(new Vector(0, -1, 0), factory.vector(0, -HALF_PI));
			assertEquals(new Vector(0, -1, 0), factory.vector(TWO_PI, -HALF_PI));
		}

		@Test
		void bottom() {
			assertEquals(new Vector(0, 1, 0), factory.vector(0, +HALF_PI));
			assertEquals(new Vector(0, 1, 0), factory.vector(TWO_PI, +HALF_PI));
		}

		@Test
		void middle() {
			assertEquals(new Vector(0, 0, -1), factory.vector(0, 0));
			assertEquals(new Vector(1, 0, 0), factory.vector(HALF_PI, 0));
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
