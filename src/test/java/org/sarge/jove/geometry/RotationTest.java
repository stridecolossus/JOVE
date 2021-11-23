package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.util.MathsUtil.PI;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Rotation.DefaultRotation;

class RotationTest {
	@Test
	void constructor() {
		final Rotation rot = new DefaultRotation(Vector.Y, PI);
		assertNotNull(rot);
		assertEquals(Vector.Y, rot.axis());
		assertEquals(PI, rot.angle());
	}

	@Nested
	class RotationMatrixTests {
		@Test
		void x() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(1, 1, -1)
					.set(2, 2, -1)
					.build();

			assertEquals(expected, Rotation.matrix(new DefaultRotation(Vector.X, PI)));
		}

		@Test
		void y() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(0, 0, -1)
					.set(2, 2, -1)
					.build();

			assertEquals(expected, Rotation.matrix(new DefaultRotation(Vector.Y, PI)));
		}

		@Test
		void z() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(0, 0, -1)
					.set(1, 1, -1)
					.build();

			assertEquals(expected, Rotation.matrix(new DefaultRotation(Vector.Z, PI)));
		}

		@Test
		void matrixInvalidArbitraryAxis() {
			assertThrows(UnsupportedOperationException.class, () -> Rotation.matrix(new DefaultRotation(new Vector(1, 2, 3), PI)));
		}
	}
}
