package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.Y;
import static org.sarge.jove.util.MathsUtil.PI;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Rotation.AxisAngle;

class RotationTest {
	@Nested
	class AxisAngleTests {
		private AxisAngle rot;

		@BeforeEach
		void before() {
			rot = new AxisAngle(Y, PI);
		}

		@Test
		void constructor() {
			assertEquals(Y, rot.axis());
			assertEquals(PI, rot.angle());
			assertNotNull(rot.matrix());
			assertEquals(rot, rot.rotation());
		}

		@Test
		void equals() {
			assertEquals(rot, rot);
			assertEquals(rot, new AxisAngle(Y, PI));
			assertNotEquals(rot, null);
			assertNotEquals(rot, new AxisAngle(Y, 0));
		}

		@Test
		void rotate() {
			final Vector vec = new Vector(1, 1, 0).normalize();
			assertEquals(new Vector(-1, 1, 0).normalize(), rot.rotate(vec));
		}

		@Test
		void arbitrary() {
			rot = new AxisAngle(new Vector(1, 2, 3), PI);
			final Quaternion q = Quaternion.of(rot.axis(), PI);
			assertEquals(q.matrix(), rot.matrix());
		}
	}
}
