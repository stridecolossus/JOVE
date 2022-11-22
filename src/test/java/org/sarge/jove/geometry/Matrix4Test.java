package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Layout;

class Matrix4Test {
	@Test
	void constructor() {
		final Matrix matrix = new Matrix4();
		assertEquals(4, matrix.order());
	}

	@Test
	void layout() {
		assertEquals(Layout.floats(4 * 4), Matrix4.LAYOUT);
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
