package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Axis.Y;
import static org.sarge.jove.util.Trigonometric.HALF_PI;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

class MutableRotationTest {
	private MutableRotation rot;

	@BeforeEach
	void before() {
		rot = new MutableRotation(Y);
	}

	@Test
	void rotation() {
		assertEquals(new AxisAngle(Y, 0), rot.toAxisAngle());
	}

	@Test
	void matrix() {
		assertEquals(Y.rotation(0), rot.matrix());
	}

	@Test
	void axis() {
		rot.set(Axis.X);
		assertEquals(new AxisAngle(Axis.X, 0), rot.toAxisAngle());
	}

	@Test
	void angle() {
		rot.set(HALF_PI);
		assertEquals(new AxisAngle(Y, HALF_PI), rot.toAxisAngle());
		assertEquals(Y.rotation(HALF_PI), rot.matrix());
	}

	@Test
	void animation() {
		rot.update(0.25f);
		assertEquals(new AxisAngle(Y, HALF_PI), rot.toAxisAngle());
	}
}
