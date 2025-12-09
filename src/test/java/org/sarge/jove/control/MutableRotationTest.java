package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.*;
import static org.sarge.jove.util.MathsUtility.PI;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.AxisAngle;

class MutableRotationTest {
	private MutableRotation rotation;

	@BeforeEach
	void before() {
		rotation = new MutableRotation(Y);
	}

	@Test
	void angle() {
		rotation.set(PI);
		assertEquals(new AxisAngle(Y, PI), rotation.toAxisAngle());
	}

	@Test
	void axis() {
		rotation.set(X);
		assertEquals(new AxisAngle(X, 0), rotation.toAxisAngle());
	}

	@Test
	void animation() {
		final Animation animation = rotation.animation();
		animation.set(0.5f);
		assertEquals(new AxisAngle(Y, PI), rotation.toAxisAngle());
	}

	@Test
	void equals() {
		assertEquals(rotation, rotation);
		assertEquals(rotation, new MutableRotation(Y));
		assertNotEquals(rotation, null);
		assertNotEquals(rotation, new MutableRotation(X));
	}
}
