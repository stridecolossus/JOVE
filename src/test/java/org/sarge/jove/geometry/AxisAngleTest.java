package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.MathsUtility.PI;

import org.junit.jupiter.api.*;

class AxisAngleTest {
	private Normal axis;
	private AxisAngle rotation;

	@BeforeEach
	void before() {
		axis = new Normal(new Vector(1, 1, 0));
		rotation = new AxisAngle(axis, PI);
	}

	@Test
	void rotation() {
		assertEquals(axis, rotation.axis());
		assertEquals(PI, rotation.angle());
	}

	@Test
	void matrix() {
		final Matrix expected = new Matrix.Builder(4)
				.set(0, 1, +1)
				.set(1, 0, +1)
				.set(2, 2, -1)
				.set(3, 3, +1)
				.build();

		assertEquals(expected, rotation.matrix());
	}

	@Test
	void rotate() {
		assertEquals(new Vector(0, 1, 0), rotation.rotate(new Vector(1, 0, 0)));
	}

	@Test
	void equals() {
		assertEquals(rotation, rotation);
		assertEquals(rotation, new AxisAngle(axis, PI));
		assertNotEquals(rotation, null);
		assertNotEquals(rotation, new AxisAngle(Axis.Z, 0));
	}
}
