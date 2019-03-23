package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Rotation.MutableRotation;
import org.sarge.jove.util.MathsUtil;

public class RotationTest {
	@Test
	public void rotation() {
		final Rotation rot = Rotation.of(Vector.Y_AXIS, MathsUtil.HALF_PI);
		assertEquals(Vector.Y_AXIS, rot.axis());
		assertEquals(MathsUtil.HALF_PI, rot.angle());
		assertEquals(Matrix.rotation(Vector.Y_AXIS, -MathsUtil.HALF_PI), rot.matrix());
	}

	@Nested
	class MutableRotationTests {
		private MutableRotation rot;

		@BeforeEach
		public void before() {
			rot = new MutableRotation(Vector.Y_AXIS, 0);
		}

		@Test
		public void constructor() {
			assertEquals(Vector.Y_AXIS, rot.axis());
			assertEquals(0, rot.angle());
			assertEquals(true, rot.isDirty());
		}

		@Test
		public void angle() {
			rot.angle(MathsUtil.HALF_PI);
			assertEquals(MathsUtil.HALF_PI, rot.angle());
		}

		@Test
		public void matrix() {
			assertEquals(Matrix.rotation(Vector.Y_AXIS, 0), rot.matrix());
			assertEquals(false, rot.isDirty());
		}
	}
}
