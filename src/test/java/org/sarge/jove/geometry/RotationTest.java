package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.util.MathsUtil.PI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Rotation.AbstractRotation;

class RotationTest {
	@Test
	void x() {
		final Matrix expected = new Matrix.Builder()
				.identity()
				.set(1, 1, -1)
				.set(2, 2, -1)
				.build();

		assertEquals(expected, Rotation.matrix(Vector.X, PI));
	}

	@Test
	void y() {
		final Matrix expected = new Matrix.Builder()
				.identity()
				.set(0, 0, -1)
				.set(2, 2, -1)
				.build();

		assertEquals(expected, Rotation.matrix(Vector.Y, PI));
	}

	@Test
	void z() {
		final Matrix expected = new Matrix.Builder()
				.identity()
				.set(0, 0, -1)
				.set(1, 1, -1)
				.build();

		assertEquals(expected, Rotation.matrix(Vector.Z, PI));
	}

	@Test
	void matrixInvalidArbitraryAxis() {
		assertThrows(UnsupportedOperationException.class, () -> Rotation.matrix(new Vector(1, 2, 3), PI));
	}

	@Nested
	class AbstractRotationTests {
		private Rotation rot;

		@BeforeEach
		void before() {
			rot = new AbstractRotation(Vector.Y, PI) {
				@Override
				public Matrix matrix() {
					return null;
				}
			};
		}

		@Test
		void constructor() {
			assertEquals(Vector.Y, rot.axis());
			assertEquals(PI, rot.angle());
		}

		@Test
		void equals() {
			assertEquals(true, rot.equals(rot));
			assertEquals(false, rot.equals(null));
		}
	}
}
