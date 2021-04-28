package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Rotation.AbstractRotation;
import org.sarge.jove.geometry.Rotation.MutableRotation;
import org.sarge.jove.util.MathsUtil;

class RotationTest {
	@Test
	void of() {
		final Rotation rot = Rotation.of(Vector.X_AXIS, MathsUtil.PI);
		assertNotNull(rot);
		assertEquals(Vector.X_AXIS, rot.axis());
		assertEquals(MathsUtil.PI, rot.angle());
	}

	@Nested
	class AbstractRotationTests {
		private AbstractRotation rot;

		@BeforeEach
		void before() {
			rot = new AbstractRotation(Vector.X_AXIS) {
				@Override
				public float angle() {
					return 0;
				}
			};
		}

		@Test
		void constructor() {
			assertEquals(Vector.X_AXIS, rot.axis());
			assertEquals(false, rot.isDirty());
			assertEquals(Quaternion.of(rot).matrix(), rot.matrix());
		}
	}

	@Nested
	class MutableRotationTests {
		private MutableRotation rot;

		@BeforeEach
		void before() {
			rot = new MutableRotation(Vector.X_AXIS);
		}

		@Test
		void constructor() {
			assertEquals(Vector.X_AXIS, rot.axis());
			assertEquals(true, rot.isDirty());
		}

		@Test
		void matrix() {
			assertEquals(Rotation.of(Vector.X_AXIS, 0).matrix(), rot.matrix());
			assertEquals(false, rot.isDirty());
		}

		@Test
		void angle() {
			rot.matrix();
			rot.angle(MathsUtil.PI);
			assertEquals(true, rot.isDirty());
			assertEquals(Rotation.of(Vector.X_AXIS, MathsUtil.PI).matrix(), rot.matrix());
			assertEquals(false, rot.isDirty());
		}
	}
}
