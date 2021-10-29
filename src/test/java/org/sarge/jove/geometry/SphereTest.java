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
import org.sarge.jove.geometry.Sphere.PointFactory;

public class SphereTest {
	@Nested
	class PointFactoryTests {
		@Test
		void top() {
			assertEquals(new Point(0, 0, -1), PointFactory.DEFAULT.point(0, -HALF_PI));
			assertEquals(new Point(0, 0, -1), PointFactory.DEFAULT.point(TWO_PI, -HALF_PI));
		}

		@Test
		void bottom() {
			assertEquals(new Point(0, 0, +1), PointFactory.DEFAULT.point(0, +HALF_PI));
			assertEquals(new Point(0, 0, +1), PointFactory.DEFAULT.point(TWO_PI, +HALF_PI));
		}

		@Test
		void middle() {
			assertEquals(new Point(-1, 0, 0), PointFactory.DEFAULT.point(PI, 0));
		}

		@Test
		void rotate() {
			final PointFactory factory = spy(PointFactory.class);
			final PointFactory rotate = factory.rotate();
			assertNotNull(rotate);
			rotate.point(0, 0);
			verify(factory).point(-HALF_PI, 0);
		}

		@Test
		void swizzle() {
			final PointFactory factory = spy(PointFactory.class);
			final PointFactory swizzle = factory.swizzle();
			assertNotNull(swizzle);
			when(factory.point(1, 2)).thenReturn(new Point(1, 2, 3));
			assertEquals(new Point(1, 3, 2), swizzle.point(1, 2));
		}
	}
}
