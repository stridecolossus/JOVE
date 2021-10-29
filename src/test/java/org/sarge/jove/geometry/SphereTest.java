package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.MathsUtil.HALF_PI;
import static org.sarge.jove.util.MathsUtil.PI;
import static org.sarge.jove.util.MathsUtil.TWO_PI;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SphereTest {
	@Nested
	class SurfacePointTests {
		@Test
		void top() {
			assertEquals(new Point(0, 0, -1), Sphere.point(0, -HALF_PI));
			assertEquals(new Point(0, 0, -1), Sphere.point(TWO_PI, -HALF_PI));
		}

		@Test
		void bottom() {
			assertEquals(new Point(0, 0, +1), Sphere.point(0, +HALF_PI));
			assertEquals(new Point(0, 0, +1), Sphere.point(TWO_PI, +HALF_PI));
		}

		@Test
		void middle() {
			assertEquals(new Point(-1, 0, 0), Sphere.point(PI, 0));
		}

		@Test
		void rotate() {
			assertEquals(Sphere.point(0, 0), Sphere.pointRotated(HALF_PI, 0));
		}

		@Test
		void swizzle() {
			assertEquals(new Point(1, 3, 2), Sphere.swizzle(new Point(1, 2, 3)));
		}
	}
}
