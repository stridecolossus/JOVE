package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.MathsUtil.HALF_PI;
import static org.sarge.jove.util.MathsUtil.PI;
import static org.sarge.jove.util.MathsUtil.TWO_PI;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Sphere.PointFunction;

public class SphereTest {
	@Nested
	class PointFunctionTests {
		@Test
		void top() {
			assertEquals(new Point(0, 0, -1), PointFunction.DEFAULT.point(0, -HALF_PI));
			assertEquals(new Point(0, 0, -1), PointFunction.DEFAULT.point(TWO_PI, -HALF_PI));
		}

		@Test
		void bottom() {
			assertEquals(new Point(0, 0, +1), PointFunction.DEFAULT.point(0, +HALF_PI));
			assertEquals(new Point(0, 0, +1), PointFunction.DEFAULT.point(TWO_PI, +HALF_PI));
		}

		@Test
		void middle() {
			assertEquals(new Point(-1, 0, 0), PointFunction.DEFAULT.point(PI, 0));
		}

		@Test
		void rotate() {
			final PointFunction func = spy(PointFunction.class);
			final PointFunction rotate = func.rotate();
			assertNotNull(rotate);
			rotate.point(0, 0);
			verify(func).point(-HALF_PI, 0);
		}

		@Test
		void swizzle() {
			final PointFunction func = spy(PointFunction.class);
			final PointFunction swizzle = func.swizzle();
			assertNotNull(swizzle);
			when(func.point(1, 2)).thenReturn(new Point(1, 2, 3));
			assertEquals(new Point(1, 3, 2), swizzle.point(1, 2));
		}
	}
}
