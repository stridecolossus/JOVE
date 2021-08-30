package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
				builder.set(c, r, index);			// Note index increments in column-major order
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
		assertEquals(ORDER * ORDER * Float.BYTES, matrix.length());
	}

	@Test
	void identity() {
		final Matrix identity = Matrix.identity(ORDER);
		assertNotNull(identity);
		assertEquals(ORDER, identity.order());
	}

	@Test
	void get() {
		int index = 1;
		for(int r = 0; r < ORDER; ++r) {
			for(int c = 0; c < ORDER; ++c) {
				assertEquals(index, matrix.get(c, r));
				++index;
			}
		}
	}

	@Test
	void getInvalidIndex() {
		assertThrows(AssertionError.class, () -> matrix.get(0, -1));
		assertThrows(AssertionError.class, () -> matrix.get(4, 0));
	}

	@Test
	void array() {
		final float[] expected = new float[ORDER * ORDER];
		for(int n = 0; n < expected.length; ++n) {
			expected[n] = n + 1;
		}
		assertArrayEquals(expected, matrix.array());
	}

	@Test
	void row() {
		assertEquals(new Vector(1, 5, 9), matrix.row(0));
		assertEquals(new Vector(2, 6, 10), matrix.row(1));
	}

	@Test
	void column() {
		assertEquals(new Vector(1, 2, 3), matrix.column(0));
		assertEquals(new Vector(5, 6, 7), matrix.column(1));
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
				assertEquals(index, transpose.get(r, c));
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
		final Matrix result = matrix.multiply(matrix);
		assertNotNull(result);
		assertEquals(ORDER, result.order());
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
		final ByteBuffer buffer = mock(ByteBuffer.class);
		matrix.buffer(buffer);
		for(int n = 0; n < ORDER * ORDER; ++n) {
			verify(buffer).putFloat(n + 1);
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
	class BuilderTests {
		private Builder builder;
		private float[] expected;

		@BeforeEach
		void before() {
			builder = new Builder(ORDER);
			expected = new float[ORDER * ORDER];
		}

		@Test
		void build() {
			final Matrix matrix = builder.build();
			assertNotNull(matrix);
			assertEquals(ORDER, matrix.order());
			assertArrayEquals(expected, matrix.array());
		}

		@Test
		void set() {
			matrix = builder.set(1, 2, 3).build();
			expected[1 + 2 * ORDER] = 3;
			assertArrayEquals(expected, matrix.array());
		}

		@Test
		void row() {
			builder.row(1, new Vector(1, 2, 3));
			expected[1] = 1;
			expected[5] = 2;
			expected[9] = 3;
			assertArrayEquals(expected, builder.build().array());
		}

		@Test
		void column() {
			builder.column(1, new Vector(1, 2, 3));
			expected[4] = 1;
			expected[5] = 2;
			expected[6] = 3;
			assertArrayEquals(expected, builder.build().array());
		}

		@Test
		void invalidOrder() {
			assertThrows(IllegalArgumentException.class, () -> new Builder(0));
			assertThrows(IllegalArgumentException.class, () -> new Builder(-1));
		}
	}
}
