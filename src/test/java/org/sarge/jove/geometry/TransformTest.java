package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TransformTest {
	@Test
	void translation() {
		final Vector vec = new Vector(1, 2, 3);

		final Matrix expected = new Matrix.Builder(4)
				.identity()
				.column(3, vec)
				.build();

		assertEquals(expected, Transform.translation(new Vector(1, 2, 3)));
	}

	@Test
	void scale() {
		final Matrix expected = new Matrix.Builder(4)
				.set(0, 0, 1)
				.set(1, 1, 2)
				.set(2, 2, 3)
				.set(3, 3, 1)
				.build();

		assertEquals(expected, Transform.scale(1, 2, 3));
	}
}
