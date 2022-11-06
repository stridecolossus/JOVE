package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Component;

class Matrix4Test {
	@Test
	void constructor() {
		final Matrix matrix = new Matrix4();
		assertEquals(4, matrix.order());
		assertEquals(4 * 4 * Float.BYTES, matrix.length());
	}

	@Test
	void layout() {
		final int len = 4 * 4 * Float.BYTES;
		assertEquals(Component.floats(4 * 4), Matrix4.LAYOUT);
		assertEquals(len, Matrix4.LENGTH);
	}

	@Test
	void translation() {
		final Matrix expected = new Matrix.Builder().identity().column(3, Axis.X).build();
		assertEquals(expected, Matrix4.translation(Axis.X));
	}

	@Test
	void scale() {
		final Matrix expected = new Matrix.Builder().identity().set(2, 2, 3).build();
		assertEquals(expected, Matrix4.scale(1, 1, 3));
	}
}
