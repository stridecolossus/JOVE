package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.MathsUtil;

public class MatrixTest {
	private Matrix matrix;

	@BeforeEach
	void before() {
		matrix = new Matrix(2, new float[]{1, 2, 3, 4});
	}

	@Test
	public void constructor() {
		assertEquals(2, matrix.order());
		assertEquals(1, matrix.get(0, 0));
		assertEquals(2, matrix.get(1, 0));
		assertEquals(3, matrix.get(0, 1));
		assertEquals(4, matrix.get(1, 1));
	}

	@Test
	public void constructorInvalidArrayDimensions() {
		assertThrows(IllegalArgumentException.class, () -> new Matrix(2, new float[]{1, 2, 3}));
		assertThrows(IllegalArgumentException.class, () -> new Matrix(new float[]{}));
	}

	@Test
	public void multiply() {
		final Matrix result = matrix.multiply(matrix);
		final Matrix expected = new Matrix(2, new float[]{7, 10, 15, 22});
		assertEquals(expected, result);
	}

	@Test
	public void multiplyIdentity() {
		assertEquals(matrix, matrix.multiply(Matrix.identity(2)));
	}

	@Test
	public void buffer() {
		final ByteBuffer buffer = ByteBuffer.allocate(2 * 2 * Float.BYTES);
		matrix.buffer(buffer);
		buffer.flip();
		for(int r = 0; r < 2; ++r) {
			for(int c = 0; c < 2; ++c) {
				assertEquals(matrix.get(c, r), buffer.getFloat());		// Note - row-column reversed
			}
		}
	}

	@Nested
	class IdentityTests {
		@Test
		public void constructor() {
			assertEquals(4, Matrix.IDENTITY.order());
			for(int r = 0; r < 4; ++r) {
				for(int c = 0; c < 4; ++c) {
					if(r == c) {
						assertEquals(1, Matrix.IDENTITY.get(r, c));
					}
					else {
						assertEquals(0, Matrix.IDENTITY.get(r, c));
					}
				}
			}
		}

		@Test
		public void transpose() {
			assertEquals(Matrix.IDENTITY, Matrix.IDENTITY.transpose());
		}

		@Test
		public void multiply() {
			assertEquals(Matrix.IDENTITY, Matrix.IDENTITY.multiply(Matrix.IDENTITY));
		}
	}

//	@Test
//	public void multiplyPoint() {
//		matrix = new Matrix.Builder()
//			.identity()
//			.set(0, 1, 2)
//			.set(0, 2, 3)
//			.set(0, 3, 4)
//			.build();
//		final Point pos = new Point(1, 2, 3);
//		assertEquals(new Point((1 * 1) + (2 * 2) + (3 * 3) + (1 * 4), 2, 3), matrix.multiply(pos));
//	}
//
//	@Test
//	public void multiplyIdentityPoint() {
//		final Point pos = new Point(1, 2, 3);
//		assertEquals(pos, Matrix.IDENTITY.multiply(pos));
//	}

	@Test
	public void equals() {
		final Matrix matrix = new Matrix(2, new float[]{1, 2, 3, 4});
		assertEquals(true, matrix.equals(matrix));
		assertEquals(true, matrix.equals(new Matrix(2, new float[]{1, 2, 3, 4})));
		assertEquals(false, matrix.equals(null));
		assertEquals(false, matrix.equals(Matrix.IDENTITY));
		assertEquals(false, matrix.equals(new Matrix(2, new float[]{4, 3, 2, 1})));
	}

	@Nested
	class TransformMethods {
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
	class BuilderTests {
		@Test
		public void invalidOrder() {
			assertThrows(IllegalArgumentException.class, () -> new Builder(0));
			assertThrows(IllegalArgumentException.class, () -> new Builder(-1));
		}

		@Test
		public void identity() {
			final Matrix result = new Builder().identity().build();
			assertEquals(Matrix.IDENTITY, result);
		}

		@Test
		public void set() {
			final Matrix result = new Builder(2).set(0, 1, 2).build();
			assertEquals(new Matrix(2, new float[]{0, 0, 2, 0}), result);
		}

		@Test
		public void row() {
			final Matrix result = new Builder(3).row(1, new Vector(1, 2, 3)).build();
			assertEquals(new Matrix(3, new float[]{0, 1, 0, 0, 2, 0, 0, 3, 0}), result);
		}

		@Test
		public void column() {
			final Matrix result = new Builder(3).column(1, new Vector(1, 2, 3)).build();
			assertEquals(new Matrix(3, new float[]{0, 0, 0, 1, 2, 3, 0, 0, 0}), result);
		}
	}
}
