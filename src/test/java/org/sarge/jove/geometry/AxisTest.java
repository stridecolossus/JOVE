package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Axis.*;
import static org.sarge.jove.util.MathsUtil.PI;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Rotation.AxisAngle;

public class AxisTest {
	@Test
	void axes() {
		assertEquals(new Vector(1, 0, 0), X);
		assertEquals(new Vector(0, 1, 0), Y);
		assertEquals(new Vector(0, 0, 1), Z);
	}

	@Test
	void x() {
		final Matrix expected = new Matrix.Builder()
				.identity()
				.set(1, 1, -1)
				.set(2, 2, -1)
				.build();

		assertEquals(expected, new AxisAngle(X, PI).matrix());
	}

	@Test
	void y() {
		final Matrix expected = new Matrix.Builder()
				.identity()
				.set(0, 0, -1)
				.set(2, 2, -1)
				.build();

		assertEquals(expected, new AxisAngle(Y, PI).matrix());
	}

	@Test
	void z() {
		final Matrix expected = new Matrix.Builder()
				.identity()
				.set(0, 0, -1)
				.set(1, 1, -1)
				.build();

		assertEquals(expected, new AxisAngle(Z, PI).matrix());
	}
}
