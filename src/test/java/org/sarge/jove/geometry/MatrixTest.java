package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.io.BufferHelper;

class MatrixTest {
	private static final int ORDER = 3;

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

	@DisplayName("A matrix has an order")
	@Test
	void order() {
		assertEquals(ORDER, matrix.order());
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
		for(int r = 0; r < ORDER; ++r) {
			for(int c = 0; c < ORDER; ++c) {
				assertEquals(index, matrix.get(r, c));
				++index;
			}
		}
	}

	@DisplayName("Out-of-bounds elements cannot be retrieved from a matrix")
	@Test
	void bounds() {
		assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(0, -1));
		assertThrows(IndexOutOfBoundsException.class, () -> matrix.get(ORDER, 0));
	}

	@DisplayName("A row of a matrix can be extracted by index")
	@Test
	void row() {
		assertEquals(new Vector(1, 2, 3), matrix.row(0));
		assertEquals(new Vector(4, 5, 6), matrix.row(1));
		assertEquals(new Vector(7, 8, 9), matrix.row(2));
	}

	@DisplayName("A column of a matrix can be extracted by index")
	@Test
	void column() {
		assertEquals(new Vector(1, 4, 7), matrix.column(0));
		assertEquals(new Vector(2, 5, 8), matrix.column(1));
		assertEquals(new Vector(3, 6, 9), matrix.column(2));
	}

	@DisplayName("A column of a matrix can be extracted by index")
	@Test
	void invalid() {
		assertThrows(IndexOutOfBoundsException.class, () -> matrix.row(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> matrix.row(ORDER));
		assertThrows(IndexOutOfBoundsException.class, () -> matrix.column(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> matrix.column(ORDER));
	}

	@DisplayName("A matrix can be transposed by swapping its rows and columns")
	@Test
	void transpose() {
		final Matrix transpose = matrix.transpose();
		assertEquals(ORDER, transpose.order());
		int index = 1;
		for(int r = 0; r < ORDER; ++r) {
			for(int c = 0; c < ORDER; ++c) {
				assertEquals(index, transpose.get(c, r));
				++index;
			}
		}
	}

	@DisplayName("A matrix can be multiplied by another matrix")
	@Test
	void multiply() {
		final Matrix result = matrix.multiply(matrix);
		assertEquals(30,  result.get(0, 0));
		assertEquals(66,  result.get(1, 0));
		assertEquals(102, result.get(2, 0));
		assertEquals(36,  result.get(0, 1));
		assertEquals(81,  result.get(1, 1));
		assertEquals(126, result.get(2, 1));
		assertEquals(42,  result.get(0, 2));
		assertEquals(96,  result.get(1, 2));
		assertEquals(150, result.get(2, 2));
	}

	@DisplayName("A matrix cannot be multiplied by another matrix with a different order")
	@Test
	void multiplyInvalid() {
		final Matrix other = new Builder(2).build();
		assertThrows(IllegalArgumentException.class, () -> matrix.multiply(other));
	}

	@Test
	void equals() {
		assertEquals(matrix, matrix);
		assertNotEquals(matrix, null);
		assertNotEquals(matrix, new Matrix.Builder(ORDER).build());
	}

	@DisplayName("A matrix can be converted to a string")
	@Test
	void dump() {
		matrix.dump();
	}

	@DisplayName("The identity matrix...")
	@Nested
	class IdentityTests {
		private Matrix identity;

		@BeforeEach
		void before() {
			identity = new Matrix.Builder(ORDER).identity().build();
		}

		@DisplayName("has a populated diagonal")
		@Test
		void constructor() {
			for(int r = 0; r < ORDER; ++r) {
				for(int c = 0; c < ORDER; ++c) {
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
		assertEquals(ORDER * ORDER * Float.BYTES, matrix.length());
	}

	@DisplayName("A matrix can be written to an NIO buffer in column-major order")
	@Test
	void buffer() {
		final ByteBuffer buffer = ByteBuffer.allocate(ORDER * ORDER * Float.BYTES);
		matrix.buffer(buffer);
		for(int r = 0; r < ORDER; ++r) {
			for(int c = 0; c < ORDER; ++c) {
				final float expected = r + c * ORDER;
				assertEquals(expected + 1, buffer.getFloat());
			}
		}
	}

	@DisplayName("A matrix can be written to a direct NIO buffer")
	@Test
	void direct() {
		final ByteBuffer bb = BufferHelper.allocate(ORDER * ORDER * Float.BYTES);
		matrix.buffer(bb);
		assertEquals(0, bb.remaining());
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
			assertEquals(ORDER, matrix.order());
		}

		@Test
		void order() {
			assertThrows(IllegalArgumentException.class, () -> new Builder(0).build());
		}

		@Test
		void identity() {
			matrix = builder.identity().build();
			for(int r = 0; r < ORDER; ++r) {
				for(int c = 0; c < ORDER; ++c) {
					final float expected = r == c ? 1 : 0;
					assertEquals(expected, matrix.get(r, c));
				}
			}
		}

		@Test
		void set() {
			matrix = builder.set(1, 2, 3).build();
			assertEquals(3, matrix.get(1, 2));
		}

		@Test
		void setBounds() {
			assertThrows(IndexOutOfBoundsException.class, () -> builder.set(-1, 0, 3));
			assertThrows(IndexOutOfBoundsException.class, () -> builder.set(0, -1, 3));
			assertThrows(IndexOutOfBoundsException.class, () -> builder.set(ORDER, 0, 3));
			assertThrows(IndexOutOfBoundsException.class, () -> builder.set(0, ORDER, 3));
		}

		@Test
		void row() {
			matrix = builder.row(1, new Vector(1, 2, 3)).build();
			assertEquals(1, matrix.get(1, 0));
			assertEquals(2, matrix.get(1, 1));
			assertEquals(3, matrix.get(1, 2));
		}

		@Test
		void rowBounds() {
			assertThrows(IndexOutOfBoundsException.class, () -> builder.row(-1, Axis.X));
			assertThrows(IndexOutOfBoundsException.class, () -> builder.row(ORDER, Axis.X));
		}

		@Test
		void column() {
			matrix = builder.column(1, new Vector(1, 2, 3)).build();
			assertEquals(1, matrix.get(0, 1));
			assertEquals(2, matrix.get(1, 1));
			assertEquals(3, matrix.get(2, 1));
		}

		@Test
		void columnBounds() {
			assertThrows(IndexOutOfBoundsException.class, () -> builder.column(-1, Axis.X));
			assertThrows(IndexOutOfBoundsException.class, () -> builder.column(ORDER, Axis.X));
		}
	}
}
