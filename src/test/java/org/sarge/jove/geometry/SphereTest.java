package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.util.MathsUtil;

public class SphereTest {
	private Sphere sphere;

	@BeforeEach
	void before() {
		sphere = new Sphere();
	}

	@Test
	void constructor() {
		assertEquals(1, sphere.radius());
	}

	@Nested
	class PointTests {
		@Test
		void point() {
			assertEquals(new Point(1, 0, 0), sphere.point(0, 0));
		}

		@Test
		void pointHorizontal() {
			assertEquals(new Point(0, 0, 1), sphere.point(MathsUtil.HALF_PI, 0));
			assertEquals(new Point(-1, 0, 0), sphere.point(MathsUtil.PI, 0));
			assertEquals(new Point(0, 0, -1), sphere.point(MathsUtil.PI + MathsUtil.HALF_PI, 0));
			assertEquals(new Point(1, 0, 0), sphere.point(MathsUtil.TWO_PI, 0));
		}

		@Test
		void pointVertical() {
			assertEquals(new Point(0, -1, 0), sphere.point(0, -MathsUtil.HALF_PI));
			assertEquals(new Point(0, +1, 0), sphere.point(0, MathsUtil.HALF_PI));
		}
	}
}
