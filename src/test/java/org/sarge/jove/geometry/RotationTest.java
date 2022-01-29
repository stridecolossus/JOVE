package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.util.MathsUtil.PI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Rotation.DefaultRotation;

class RotationTest {
	@Nested
	class RotationMatrixTests {
		@Test
		void x() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(1, 1, -1)
					.set(2, 2, -1)
					.build();

			assertEquals(expected, new DefaultRotation(Vector.X, PI).matrix());
		}

		@Test
		void y() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(0, 0, -1)
					.set(2, 2, -1)
					.build();

			assertEquals(expected, new DefaultRotation(Vector.Y, PI).matrix());
		}

		@Test
		void z() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(0, 0, -1)
					.set(1, 1, -1)
					.build();

			assertEquals(expected, new DefaultRotation(Vector.Z, PI).matrix());
		}

		@Test
		void matrixInvalidArbitraryAxis() {
			assertThrows(UnsupportedOperationException.class, () -> new DefaultRotation(new Vector(1, 2, 3), PI).matrix());
		}
	}

	@Nested
	class DefaultRotationTest {
		private Rotation rot;

		@BeforeEach
		void before() {
			rot = new DefaultRotation(Vector.Y, PI);
		}

		@Test
		void constructor() {
			assertEquals(Vector.Y, rot.axis());
			assertEquals(PI, rot.angle());
			assertEquals(Rotation.matrix(Vector.Y, PI), rot.matrix());
		}

		@Test
		void equals() {
			assertEquals(rot, rot);
			assertEquals(rot, new DefaultRotation(Vector.Y, PI));
			assertNotEquals(rot, null);
			assertNotEquals(rot, new DefaultRotation(Vector.Y, 0));
		}
	}
}
