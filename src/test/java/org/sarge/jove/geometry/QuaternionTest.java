package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.Y;
import static org.sarge.jove.util.MathsUtility.PI;

import org.junit.jupiter.api.*;

public class QuaternionTest {
	private AxisAngle rot;
	private Quaternion quaternion;

	@BeforeEach
	void before() {
		rot = new AxisAngle(Y, PI);
		quaternion = Quaternion.of(rot);
	}

	@Test
	void toAxisAngle() {
		assertEquals(rot, quaternion.toAxisAngle());
	}

	@Test
	void conjugate() {
		final Quaternion conjugate = quaternion.conjugate();
		assertEquals(new AxisAngle(Y.invert(), PI), conjugate.toAxisAngle());
	}

	@Test
	void inverse() {
		assertEquals(quaternion, quaternion.conjugate().conjugate());
	}

	@Test
	void multiply() {
		final var x = new AxisAngle(Axis.X, PI);
		final var result = quaternion.multiply(Quaternion.of(x));
		final Matrix expected = rot.matrix().multiply(x.matrix());
		assertEquals(expected, result.matrix());
	}

	@Test
	void matrix() {
		assertEquals(rot.matrix(), quaternion.matrix());
	}

	@Test
	void rotate() {
		assertEquals(new Vector(-1, 0, 0), quaternion.rotate(new Vector(1, 0, 0)));
	}

	@Test
	void equals() {
		assertEquals(quaternion, quaternion);
		assertEquals(quaternion, Quaternion.of(rot));
		assertNotEquals(quaternion, null);
		assertNotEquals(quaternion, Quaternion.IDENTITY);
	}
}
