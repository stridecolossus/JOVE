package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Matrix.Builder;

class MatrixTest {
	private static final int ORDER = 4;

	private Matrix matrix;

	@BeforeEach
	void before() {
		final Builder builder = new Builder(ORDER);
		int index = 1;
		for(int r = 0; r < ORDER; ++r) {
			for(int c = 0; c < ORDER; ++c) {
				builder.set(r, c, index);
				++index;
			}
		}
		matrix = builder.build();
	}

	@Test
	void constructor() {
		assertNotNull(matrix);
		assertEquals(ORDER, matrix.order());
		assertEquals(matrix, matrix.matrix());
		assertEquals(false, matrix.isDirty());
	}

	@Test
	void get() {
		int index = 1;
		for(int r = 0; r < ORDER; ++r) {
			for(int c = 0; c < ORDER; ++c) {
				assertEquals(index, matrix.get(r, c));
				++index;
			}
		}
	}

	@Test
	void getInvalidIndex() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> matrix.get(0, -1));
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> matrix.get(4, 0));
	}

	@Test
	void row() {
		assertEquals(new Vector(1, 2, 3), matrix.row(0));
		assertEquals(new Vector(5, 6, 7), matrix.row(1));
	}

	@Test
	void column() {
		assertEquals(new Vector(1, 5, 9), matrix.column(0));
		assertEquals(new Vector(2, 6, 10), matrix.column(1));
	}

	@Test
	void transpose() {
		// Transpose
		final Matrix transpose = matrix.transpose();
		assertNotNull(transpose);
		assertEquals(ORDER, transpose.order());

		// Check transposed matrix
		int index = 1;
		for(int r = 0; r < ORDER; ++r) {
			for(int c = 0; c < ORDER; ++c) {
				assertEquals(index, transpose.get(c, r));
				++index;
			}
		}
	}

	@Test
	void transposeIdentity() {
		final Matrix identity = Matrix.identity(ORDER);
		assertEquals(identity, identity.transpose());
	}

	@Test
	void multiply() {
		// Create a 180 degree rotation about the X axis
		final Matrix rot = new Matrix.Builder()
				.identity()
				.set(1, 1, 1)
				.set(1, 2, -0)
				.set(2, 1, 0)
				.set(2, 2, 1)
				.build();

		// Create translation matrix
		final Matrix trans = new Matrix.Builder()
				.identity()
				.column(3, new Vector(1, 2, 3))
				.build();

		// Check vs expected result
		final Matrix expected = new Matrix.Builder()
				.identity()
				.column(3, new Vector(1, 2, 3))
				.build();

		assertEquals(expected, rot.multiply(trans));
	}

	@Test
	void multiplyIdentity() {
		final Matrix identity = Matrix.identity(ORDER);
		assertEquals(identity, identity.multiply(identity));
	}

	@Test
	void multiplyInvalidOrder() {
		final Matrix other = new Builder(3).build();
		assertThrows(IllegalArgumentException.class, () -> matrix.multiply(other));
	}

	@Test
	void length() {
		assertEquals(ORDER * ORDER * Float.BYTES, matrix.length());
	}

	@Test
	void buffer() {
		// Buffer matrix
		final ByteBuffer buffer = ByteBuffer.allocate(ORDER * ORDER * Float.BYTES);
		matrix.buffer(buffer);
		assertEquals(0, buffer.remaining());

		// Check matrix written in column-major order
		buffer.rewind();
		for(int r = 0; r < ORDER; ++r) {
			for(int c = 0; c < ORDER; ++c) {
				assertEquals(1 + r + c * ORDER, buffer.getFloat());
			}
		}
	}

	@Test
	void equals() {
		assertEquals(true, matrix.equals(matrix));
		assertEquals(false, matrix.equals(null));
		assertEquals(false, matrix.equals(new Builder(ORDER).build()));
		assertEquals(false, matrix.equals(new Builder(3).build()));
	}

	@Nested
	class FactoryMethodTests {
		@Test
		void identity() {
			final Matrix identity = Matrix.identity(ORDER);
			assertNotNull(identity);
			assertEquals(ORDER, identity.order());
			assertEquals(Matrix.IDENTITY, identity);
			for(int r = 0; r < ORDER; ++r) {
				for(int c = 0; c < ORDER; ++c) {
					final float expected = r == c ? 1 : 0;
					assertEquals(expected, identity.get(r, c));
				}
			}
		}

		@Test
		void translation() {
			final Matrix expected = new Matrix.Builder().identity().column(3, Vector.X).build();
			assertEquals(expected, Matrix.translation(Vector.X));
		}

		@Test
		void scale() {
			final Matrix expected = new Matrix.Builder().identity().set(2, 2, 3).build();
			assertEquals(expected, Matrix.scale(1, 1, 3));
		}
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder(ORDER);
		}

		@Test
		void build() {
			final Matrix matrix = builder.build();
			assertNotNull(matrix);
			assertEquals(ORDER, matrix.order());
		}

		@Test
		void set() {
			matrix = builder.set(1, 2, 3).build();
			assertEquals(3, matrix.get(1, 2));
		}

		@Test
		void row() {
			matrix = builder.row(1, new Vector(1, 2, 3)).build();
			assertEquals(1, matrix.get(1, 0));
			assertEquals(2, matrix.get(1, 1));
			assertEquals(3, matrix.get(1, 2));
		}

		@Test
		void column() {
			matrix = builder.column(1, new Vector(1, 2, 3)).build();
			assertEquals(1, matrix.get(0, 1));
			assertEquals(2, matrix.get(1, 1));
			assertEquals(3, matrix.get(2, 1));
		}

		@Test
		void invalidOrder() {
			assertThrows(IllegalArgumentException.class, () -> new Builder(0));
			assertThrows(IllegalArgumentException.class, () -> new Builder(-1));
		}
	}
}
