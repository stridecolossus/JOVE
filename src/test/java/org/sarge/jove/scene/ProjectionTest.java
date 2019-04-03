package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;

public class ProjectionTest {
	private Dimensions dim;

	@BeforeEach
	public void before() {
		dim = new Dimensions(640, 480);
	}

	@Test
	public void perspective() {
		final Projection perspective = Projection.DEFAULT;
		System.out.println(perspective.matrix(0.1f, 1000f, dim));
		final Matrix expected = new Matrix.Builder()
			.set(0, 0, 0.75f)
			.set(1, 1, -1)
			.set(2, 2, -1.0001f)
			.set(2, 3, -1)
			.set(3, 2, -0.10001f)
			.build();
		assertFloatEquals(1, perspective.height(dim));
		assertEquals(expected, perspective.matrix(0.1f, 1000f, dim));
	}

	@Test
	public void orthographic() {
		final Projection flat = Projection.FLAT;
		assertEquals(480f, flat.height(dim), 0.0001f);
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
