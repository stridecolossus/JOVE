package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.Y;
import static org.sarge.jove.util.Trigonometric.PI;

import org.junit.jupiter.api.*;

class AxisAngleTest {
	private AxisAngle rot;

	@BeforeEach
	void before() {
		rot = new AxisAngle(Y, PI);
	}

	@Test
	void constructor() {
		assertEquals(Y, rot.axis());
		assertEquals(PI, rot.angle());
	}

	@Test
	void rotation() {
		assertEquals(rot, rot.toAxisAngle());
	}

	@Test
	void matrix() {
		assertEquals(Y.rotation(PI), rot.matrix());
	}

	@Test
	void equals() {
		assertEquals(rot, rot);
		assertEquals(rot, new AxisAngle(Y, PI));
		assertNotEquals(rot, null);
		assertNotEquals(rot, new AxisAngle(Y, 0));
	}
}
