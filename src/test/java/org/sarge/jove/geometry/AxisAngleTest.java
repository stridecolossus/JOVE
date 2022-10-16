package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.Y;
import static org.sarge.jove.util.MathsUtil.PI;

import org.junit.jupiter.api.*;

class AxisAngleTest {
	private AxisAngle rot;

	@BeforeEach
	void before() {
		rot = AxisAngle.of(Y, PI);
	}

	@Test
	void constructor() {
		assertEquals(Y, rot.axis());
		assertEquals(PI, rot.angle());
		assertNotNull(rot.matrix());
		assertSame(rot, rot.toAxisAngle());
	}

	@Test
	void equals() {
		assertEquals(rot, rot);
		assertEquals(rot, AxisAngle.of(Y, PI));
		assertNotEquals(rot, null);
		assertNotEquals(rot, AxisAngle.of(Y, 0));
	}

	@Test
	void rotate() {
		final Vector vec = new Vector(1, 1, 0).normalize();
		assertEquals(new Vector(-1, 1, 0).normalize(), rot.rotate(vec));
	}

	@Test
	void matrix() {
		assertEquals(Y.rotation(PI), rot.matrix());
	}
}
