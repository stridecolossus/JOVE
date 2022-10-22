package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Component;
import org.sarge.jove.geometry.Matrix.*;

class MatrixTest {
	private Matrix matrix;

	@BeforeEach
	void before() {
		final Builder builder = new Builder();
		int index = 1;
		for(int r = 0; r < 4; ++r) {
			for(int c = 0; c < 4; ++c) {
				builder.set(r, c, index);
				++index;
			}
		}
		matrix = builder.build();
		assertNotNull(matrix);
	}

	@DisplayName("A matrix has an order")
	@Test
	void order() {
		assertEquals(4, matrix.order());
	}

	@DisplayName("A matrix is a transform")
	@Test
	void transform() {
		assertEquals(matrix, matrix.matrix());
		assertEquals(false, matrix.isMutable());
	}

	@DisplayName("The elements of a matrix can be retrieved by row and column")
	@Test
	void get() {
		int index = 1;
		for(int r = 0; r < 4; ++r) {
			for(int c = 0; c < 4; ++c) {
				assertEquals(index, matrix.get(r, c));
				++index;
			}
		}
	}

	@DisplayName("Out-of-bounds elements cannot be retrieved from a matrix")
	@Test
	void bounds() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> matrix.get(0, -1));
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> matrix.get(4, 0));
	}

	@DisplayName("A row of a matrix can be extracted by index")
	@Test
	void row() {
		assertEquals(new Vector(1, 2, 3), matrix.row(0));
		assertEquals(new Vector(5, 6, 7), matrix.row(1));
	}

	@DisplayName("A column of a matrix can be extracted by index")
	@Test
	void column() {
		assertEquals(new Vector(1, 5, 9), matrix.column(0));
		assertEquals(new Vector(2, 6, 10), matrix.column(1));
	}

	@DisplayName("A matrix can be transposed by swapping its rows and columns")
	@Test
	void transpose() {
		// Transpose
		final Matrix transpose = matrix.transpose();
		assertNotNull(transpose);
		assertEquals(4, transpose.order());

		// Check transposed matrix
		int index = 1;
		for(int r = 0; r < 4; ++r) {
			for(int c = 0; c < 4; ++c) {
				assertEquals(index, transpose.get(c, r));
				++index;
			}
		}
	}

	@DisplayName("A matrix can be multiplied by another matrix")
	@Test
	void multiply() {
		final Matrix result = matrix.multiply(matrix);
		assertNotNull(result);
		assertEquals(90, result.get(0, 0));
	}

	@DisplayName("A matrix cannot be multiplied by another matrix with a different order")
	@Test
	void multiplyInvalid() {
		final Matrix other = new Builder(3).build();
		assertThrows(IllegalArgumentException.class, () -> matrix.multiply(other));
	}

	@Test
	void equals() {
		assertEquals(matrix, matrix);
		assertNotEquals(matrix, null);
		assertNotEquals(matrix, Matrix4.IDENTITY);
	}

	@DisplayName("The identity matrix...")
	@Nested
	class IdentityTests {
		private Matrix identity;

		@BeforeEach
		void before() {
			identity = new Matrix.Builder().identity().build();
			assertNotNull(identity);
		}

		@DisplayName("has a populated diagonal")
		@Test
		void constructor() {
			for(int r = 0; r < 4; ++r) {
				for(int c = 0; c < 4; ++c) {
					final float expected = r == c ? 1 : 0;
					assertEquals(expected, identity.get(r, c));
				}
			}
		}

		@DisplayName("is the transpose of itself")
		@Test
		void transpose() {
			assertEquals(identity, identity.transpose());
		}

		@DisplayName("does not affect a multiplied matrix")
		@Test
		void multiply() {
			assertEquals(matrix, matrix.multiply(identity));
			assertEquals(matrix, identity.multiply(matrix));
			assertEquals(identity, identity.multiply(identity));
		}
	}

	@DisplayName("A matrix has a length in bytes")
	@Test
	void length() {
		assertEquals(4 * 4 * Float.BYTES, matrix.length());
	}

	@DisplayName("A matrix can be written to an NIO buffer in column-major order")
	@Test
	void buffer() {
		// Buffer matrix
		final ByteBuffer buffer = ByteBuffer.allocate(4 * 4 * Float.BYTES);
		matrix.buffer(buffer);
		assertEquals(0, buffer.remaining());

		// Check matrix written in column-major order
		buffer.rewind();
		for(int r = 0; r < 4; ++r) {
			for(int c = 0; c < 4; ++c) {
				assertEquals(1 + r + c * 4, buffer.getFloat());
			}
		}
	}

	@Nested
	class TransformationMatrixTests {
		@Test
		void order() {
			assertEquals(4, Matrix4.ORDER);
			assertEquals(4, matrix.order());
		}

		@Test
		void layout() {
			final int len = 4 * 4 * Float.BYTES;
			assertEquals(Component.floats(4 * 4), Matrix4.LAYOUT);
			assertEquals(len, Matrix4.LENGTH);
		}

		@Test
		void translation() {
			final Matrix expected = new Matrix.Builder().identity().column(3, Axis.X.vector()).build();
			assertEquals(expected, Matrix4.translation(Axis.X.vector()));
		}

		@Test
		void scale() {
			final Matrix expected = new Matrix.Builder().identity().set(2, 2, 3).build();
			assertEquals(expected, Matrix4.scale(1, 1, 3));
		}
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
		}

		@Test
		void build() {
			final Matrix matrix = builder.build();
			assertNotNull(matrix);
			assertEquals(4, matrix.order());
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
			assertThrows(IllegalArgumentException.class, () -> new Matrix(new float[0][0]));
		}
	}
}
