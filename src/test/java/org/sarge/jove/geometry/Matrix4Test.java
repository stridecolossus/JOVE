package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Layout;

class Matrix4Test {
	@Test
	void identity() {
		final Matrix identity = new Matrix.Builder(4).identity().build();
		assertEquals(identity, Matrix4.IDENTITY);
	}

	@Test
	void layout() {
		assertEquals(Layout.floats(4 * 4), Matrix4.LAYOUT);
	}

	@Test
	void translation() {
		final Matrix expected = Matrix4.builder().identity().column(3, Axis.X).build();
		assertEquals(expected, Matrix4.translation(Axis.X));
	}

	@Test
	void scale() {
		final Matrix expected = Matrix4.builder().identity().set(2, 2, 3).build();
		assertEquals(expected, Matrix4.scale(1, 1, 3));
	}
}
