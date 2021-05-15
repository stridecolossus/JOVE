package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.geometry.Matrix4.ORDER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.MathsUtil;

class Matrix4Test {
	private Matrix4 matrix;

	@BeforeEach
	void before() {
		matrix = new Matrix4(new float[ORDER * ORDER]);
	}

	@Test
	void constructor() {
		assertEquals(ORDER, matrix.order());
		assertArrayEquals(new float[ORDER * ORDER], matrix.array());
		assertEquals(ORDER * ORDER * Float.BYTES, matrix.length());
		assertEquals(matrix, matrix.matrix());
	}

	@Test
	void identity() {
		assertEquals(Matrix.identity(ORDER), Matrix4.IDENTITY);
	}

	@Test
	void builder() {
		final Builder builder = Matrix4.builder();
		assertNotNull(builder);
		assertNotNull(builder.build());
		assertEquals(ORDER, builder.build().order());
	}

	@Test
	void translation() {
		final Matrix expected = Matrix4.builder().identity().column(3, Vector.X).build();
		assertEquals(expected, Matrix4.translation(Vector.X));
	}

	@Test
	void scale() {
		final Matrix expected = Matrix4.builder().identity().set(2, 2, 3).build();
		assertEquals(expected, Matrix4.scale(1, 1, 3));
	}

	@Test
	void rotation() {
		final Matrix expected = Matrix4
				.builder()
				.identity()
				.set(1, 1, MathsUtil.cos(MathsUtil.HALF))
				.set(1, 2, MathsUtil.sin(MathsUtil.HALF))
				.set(2, 1, -MathsUtil.sin(MathsUtil.HALF))
				.set(2, 2, MathsUtil.cos(MathsUtil.HALF))
				.build();
		assertEquals(expected, Matrix4.rotation(Vector.X, MathsUtil.HALF));
	}

	@Test
	void rotationInvalidAxis() {
		assertThrows(UnsupportedOperationException.class, () -> Matrix4.rotation(new Vector(1, 2, 3), MathsUtil.HALF));
	}
}