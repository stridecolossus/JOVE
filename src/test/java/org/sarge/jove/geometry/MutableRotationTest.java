package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Axis.Y;
import static org.sarge.jove.util.MathsUtil.HALF;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Rotation.AxisAngle;

public class MutableRotationTest {
	private MutableRotation rot;

	@BeforeEach
	void before() {
		rot = new MutableRotation(Y);
	}

	@Test
	void constructor() {
		assertEquals(0, rot.angle());
		assertEquals(true, rot.isDirty());
		assertEquals(new AxisAngle(Y, 0), rot.rotation());
	}

	@Test
	void matrix() {
		assertEquals(Quaternion.of(Y, 0).matrix(), rot.matrix());
		assertEquals(false, rot.isDirty());
	}

	@Test
	void angle() {
		rot.angle(HALF);
		assertEquals(HALF, rot.angle());
		assertEquals(Quaternion.of(Y, HALF).matrix(), rot.matrix());
		assertEquals(false, rot.isDirty());
		assertEquals(new AxisAngle(Y, HALF), rot.rotation());
	}
}
