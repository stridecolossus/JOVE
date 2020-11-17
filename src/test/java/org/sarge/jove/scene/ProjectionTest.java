package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.util.MathsUtil;

public class ProjectionTest {
	private Dimensions dim;

	@BeforeEach
	public void before() {
		dim = new Dimensions(640, 480);
	}

	@Test
	public void perspective() {
		final Matrix expected = new Matrix.Builder()
			.set(0, 0, 0.75f)
			.set(1, 1, -1)
			.set(2, 2, -1.0001f)
			.set(2, 3, -0.10001f)
			.set(3, 2, -1)
			.build();

		final Projection proj = Projection.perspective(MathsUtil.HALF_PI);
		assertEquals(1, proj.height(dim));
		assertEquals(expected, proj.matrix(0.1f, 1000f, dim));
	}

	@Test
	public void defaultPerspective() {
		final float expected = MathsUtil.tan(MathsUtil.toRadians(60) * MathsUtil.HALF);
		assertEquals(expected, Projection.DEFAULT.height(dim));
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
