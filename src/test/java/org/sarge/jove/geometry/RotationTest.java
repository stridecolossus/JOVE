package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.util.MathsUtil.HALF;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Rotation.AbstractRotation;
import org.sarge.jove.geometry.Rotation.MutableRotation;
import org.sarge.jove.util.MathsUtil;

class RotationTest {
	@Test
	void matrix() {
		final Matrix expected = new Matrix.Builder()
				.identity()
				.set(1, 1, MathsUtil.cos(HALF))
				.set(1, 2, MathsUtil.sin(HALF))
				.set(2, 1, -MathsUtil.sin(HALF))
				.set(2, 2, MathsUtil.cos(HALF))
				.build();

		assertEquals(expected, Rotation.matrix(Vector.X, HALF));
	}

	@Test
	void matrixInvalidArbitraryAxis() {
		assertThrows(UnsupportedOperationException.class, () -> Rotation.matrix(new Vector(1, 2, 3), HALF));
	}

	@Nested
	class AbstractRotationTests {
		private Rotation rot;

		@BeforeEach
		void before() {
			rot = new AbstractRotation(Vector.Y, HALF) {
				@Override
				public Matrix matrix() {
					return null;
				}
			};
		}

		@Test
		void constructor() {
			assertEquals(Vector.Y, rot.axis());
			assertEquals(HALF, rot.angle());
		}

		@Test
		void equals() {
			assertEquals(true, rot.equals(rot));
			assertEquals(false, rot.equals(null));
		}
	}

	@Nested
	class MutableRotationTests {
		private MutableRotation rot;

		@BeforeEach
		void before() {
			rot = new MutableRotation(Vector.Y);
		}

		@Test
		void constructor() {
			assertEquals(0, rot.angle());
			assertEquals(true, rot.isDirty());
		}

		@Test
		void constructorArbitraryAxis() {
			final Vector axis = new Vector(1, 2, 3).normalize();
			rot = new MutableRotation(axis);
			assertEquals(axis, rot.axis());
			assertNotNull(rot.matrix());
		}

		@Test
		void matrix() {
			assertEquals(Rotation.matrix(Vector.Y, 0), rot.matrix());
			assertEquals(false, rot.isDirty());
		}

		@Test
		void angle() {
			rot.angle(HALF);
			assertEquals(Rotation.matrix(Vector.Y, HALF), rot.matrix());
		}

		@Test
		void equals() {
			assertEquals(true, rot.equals(rot));
			assertEquals(true, rot.equals(new MutableRotation(Vector.Y)));
			assertEquals(false, rot.equals(null));
			assertEquals(false, rot.equals(new MutableRotation(Vector.Z)));
		}
	}
}
