package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.util.MathsUtility;

class ProjectionTest {
	private Viewport viewport;

	@BeforeEach
	public void before() {
		viewport = new Viewport(new Dimensions(640, 480));
	}

	@Test
	public void perspective() {
		final Matrix expected = new Matrix.Builder()
				.set(0, 0, 0.75f)
				.set(1, 1, 1)
				.set(2, 2, 1.001f)
				.set(2, 3, -0.1001f)
				.set(3, 2, 1)
				.build();

		final Projection projection = Projection.perspective(MathsUtility.HALF_PI);
//		assertEquals(1, proj.height(dim));
		assertEquals(expected, projection.matrix(viewport));
	}

	@Test
	public void orthographic() {
		final Projection flat = Projection.FLAT;
//		assertEquals(480f, flat.height(dim), 0.0001f);
//		System.out.println(flat.matrix(1, 100, dim));
//		// TODO
//		final Matrix expected = new Matrix.Builder()
//			.identity()
//			.set(0, 0, 0.75f)
//			.set(2, 2, -1.0002f)
//			.set(2, 3, -0.20002f)
//			.set(3, 2, -1f)
//			.build();
//		assertEquals(expected, perspective.matrix(0.1f, 1000f, dim));
	}
}
