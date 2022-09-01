package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.MathsUtil.PI;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Rotation.AxisAngle;

class RotationTest {
	@Nested
	class AxisAngleTests {
		private AxisAngle rot;

		@BeforeEach
		void before() {
			rot = new AxisAngle(Vector.Y, PI);
		}

		@Test
		void constructor() {
			assertEquals(Vector.Y, rot.axis());
			assertEquals(PI, rot.angle());
			assertNotNull(rot.matrix());
			assertEquals(rot, rot.rotation());
		}

		@Test
		void equals() {
			assertEquals(rot, rot);
			assertEquals(rot, new AxisAngle(Vector.Y, PI));
			assertNotEquals(rot, null);
			assertNotEquals(rot, new AxisAngle(Vector.Y, 0));
		}

		@Test
		void rotate() {
			final Vector vec = new Vector(1, 1, 0).normalize();
			assertEquals(new Vector(-1, 1, 0).normalize(), rot.rotate(vec));
			assertEquals(Vector.X.invert().normalize(), rot.rotate(Vector.X));
		}

		@Test
		void x() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(1, 1, -1)
					.set(2, 2, -1)
					.build();

			assertEquals(expected, new AxisAngle(Vector.X, PI).matrix());
		}

		@Test
		void y() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(0, 0, -1)
					.set(2, 2, -1)
					.build();

			assertEquals(expected, new AxisAngle(Vector.Y, PI).matrix());
		}

		@Test
		void z() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(0, 0, -1)
					.set(1, 1, -1)
					.build();

			assertEquals(expected, new AxisAngle(Vector.Z, PI).matrix());
		}

		@Test
		void arbitrary() {
			assertThrows(UnsupportedOperationException.class, () -> new AxisAngle(new Vector(1, 2, 3), PI).matrix());
		}
	}
}
