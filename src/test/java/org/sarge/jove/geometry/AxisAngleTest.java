package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.MathsUtility.PI;

import org.junit.jupiter.api.*;

class AxisAngleTest {
	private Normal axis;
	private AxisAngle rot;

	@BeforeEach
	void before() {
		axis = new Normal(new Vector(1, 1, 0));
		rot = new AxisAngle(axis, PI);
	}

	@Test
	void rotation() {
		assertEquals(rot, rot.toAxisAngle());
	}

	@Test
	void matrix() {
		final Matrix expected = new Matrix.Builder(4)
				.set(0, 1, +1)
				.set(1, 0, +1)
				.set(2, 2, -1)
				.set(3, 3, +1)
				.build();

		assertEquals(expected, rot.matrix());
	}

	@Test
	void cardinal() {
		rot = new AxisAngle(Axis.Y, PI);
		assertEquals(Axis.Y.rotation(new Angle(PI)), rot.matrix());
	}

	@Test
	void rotate() {
		assertEquals(new Vector(0, 1, 0), rot.rotate(new Vector(1, 0, 0)));
	}

	@Test
	void equals() {
		assertEquals(rot, rot);
		assertEquals(rot, new AxisAngle(axis, PI));
		assertNotEquals(rot, null);
		assertNotEquals(rot, new AxisAngle(axis, 0));
	}
}
