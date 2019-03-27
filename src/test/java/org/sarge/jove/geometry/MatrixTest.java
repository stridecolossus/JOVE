package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.nio.FloatBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.MathsUtil;

public class MatrixTest {
	private Matrix matrix;
	private Matrix identity;

	@BeforeEach
	public void before() {
		matrix = new Matrix(new float[]{1, 2, 3, 4});
		identity = Matrix.identity(4);
	}

	@Test
	public void constructor() {
		assertEquals(2, matrix.order());
		assertEquals(1, matrix.get(0, 0), 0.0001f);
		assertEquals(2, matrix.get(0, 1), 0.0001f);
		assertEquals(3, matrix.get(1, 0), 0.0001f);
		assertEquals(4, matrix.get(1, 1), 0.0001f);
		assertEquals(matrix, matrix.matrix());
		assertEquals(2 * 2, matrix.size());
	}

	@Test
	public void constructorInvalidArrayDimensions() {
		assertThrows(IllegalArgumentException.class, () -> new Matrix(new float[]{1, 2, 3}));
		assertThrows(IllegalArgumentException.class, () -> new Matrix(new float[]{}));
	}

	@Test
	public void buffer() {
		final FloatBuffer buffer = FloatBuffer.allocate(2 * 2);
		matrix.buffer(buffer);
		buffer.flip();
		for(int n = 0; n < 4; ++n) {
			assertFloatEquals(n + 1, buffer.get());
		}
	}

	@Test
	public void transpose() {
		assertEquals(new Matrix(new float[]{1, 3, 2, 4}), matrix.transpose());
		assertEquals(identity, identity.transpose());
	}

	@Test
	public void multiply() {
		// TODO
	}

	@Test
	public void multiplyIndentity() {
		assertEquals(identity, identity.multiply(identity));
	}

	@Test
	public void equals() {
		assertTrue(matrix.equals(matrix));
		assertTrue(matrix.equals(new Matrix(new float[]{1, 2, 3, 4})));
		assertFalse(matrix.equals(null));
		assertFalse(matrix.equals(new Matrix(new float[]{1})));
		assertFalse(matrix.equals(new Matrix(new float[]{1, 2, 3, 999})));
	}

	@Nested
	class FactoryMethods {
		@Test
		public void identity() {
			assertEquals(new Matrix(new float[]{1, 0, 0, 1}), Matrix.identity(2));
		}

		@Test
		public void translation() {
			final Matrix expected = new Builder().identity().column(3, Vector.X_AXIS).build();
			assertEquals(expected, Matrix.translation(Vector.X_AXIS));
		}

		@Test
		public void scale() {
			final Matrix expected = new Builder().identity().set(2, 2, 3).build();
			assertEquals(expected, Matrix.scale(new Tuple(1, 1, 3)));
		}

		@Test
		public void rotation() {
			final Matrix expected = new Builder()
				.identity()
				.set(1, 1, MathsUtil.cos(MathsUtil.HALF))
				.set(1, 2, MathsUtil.sin(MathsUtil.HALF))
				.set(2, 1, -MathsUtil.sin(MathsUtil.HALF))
				.set(2, 2, MathsUtil.cos(MathsUtil.HALF))
				.build();
			assertEquals(expected, Matrix.rotation(Vector.X_AXIS, MathsUtil.HALF));
		}

		@Test
		public void rotationInvalidAxis() {
			assertThrows(UnsupportedOperationException.class, () -> Matrix.rotation(new Vector(1, 2, 3), MathsUtil.HALF));
		}
	}

	@Nested
	class BuilderTest {
		@Test
		public void invalidOrder() {
			assertThrows(IllegalArgumentException.class, () -> new Builder(0));
			assertThrows(IllegalArgumentException.class, () -> new Builder(-1));
		}

		@Test
		public void identity() {
			final Matrix result = new Builder(2).identity().build();
			assertEquals(new Matrix(new float[]{1, 0, 0, 1}), result);
		}

		@Test
		public void set() {
			final Matrix result = new Builder(2).set(0, 1, 2).build();
			assertEquals(new Matrix(new float[]{0, 2, 0, 0}), result);
		}

		@Test
		public void row() {
			final Matrix result = new Builder(3).row(1, new Vector(1, 2, 3)).build();
			assertEquals(new Matrix(new float[]{0, 0, 0, 1, 2, 3, 0, 0, 0}), result);
		}

		@Test
		public void column() {
			final Matrix result = new Builder(3).column(1, new Vector(1, 2, 3)).build();
			assertEquals(new Matrix(new float[]{0, 1, 0, 0, 2, 0, 0, 3, 0}), result);
		}
	}
}
