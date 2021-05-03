package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultMatrixTest {
	private static final int ORDER = 4;

	private DefaultMatrix matrix;
	private float[] array;

	@BeforeEach
	void before() {
		array = new float[ORDER * ORDER];
		for(int n = 0; n < array.length; ++n) {
			array[n] = n;
		}
		matrix = new DefaultMatrix(ORDER, array);
	}

	@Test
	void constructor() {
		assertEquals(ORDER, matrix.order());
		assertEquals(matrix, matrix.matrix());
	}

	@Test
	void constructorInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultMatrix(1, new float[2]));
	}

	@Test
	void order() {
		for(int n = 1; n <= 5; ++n) {
			final Matrix m = new DefaultMatrix(new float[n * n]);
			assertEquals(n, m.order());
		}
	}

	@Test
	void array() {
		assertArrayEquals(array, matrix.array());
	}

	@Test
	void get() {
		int index = 0;
		for(int r = 0; r < ORDER; ++r) {
			for(int c = 0; c < ORDER; ++c) {
				assertEquals(index, matrix.get(c, r));		// Note - reversed indices
				++index;
			}
		}
	}

	@Test
	void getInvalidIndex() {
		assertThrows(IllegalArgumentException.class, () -> matrix.get(4, 0));
	}

	@Test
	void row() {
		assertEquals(new Vector(1, 5, 9), matrix.row(1));
	}

	@Test
	void column() {
		assertEquals(new Vector(4, 5, 6), matrix.column(1));
	}

	@Test
	void transpose() {
		// Transpose matrix
		final Matrix transpose = matrix.transpose();
		assertNotNull(transpose);
		assertEquals(ORDER, transpose.order());

		// Transpose back
		assertEquals(matrix, transpose.transpose());

		// Check transposed data
		int index = 0;
		for(int r = 0; r < ORDER; ++r) {
			for(int c = 0; c < ORDER; ++c) {
				assertEquals(index, matrix.get(c, r));
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
		final Matrix other = new DefaultMatrix(new float[1]);
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
			verify(buffer).putFloat(n);
		}
	}

	@Test
	void equals() {
		assertEquals(true, matrix.equals(matrix));
		assertEquals(true, matrix.equals(new DefaultMatrix(array)));
		assertEquals(false, matrix.equals(null));
		assertEquals(false, matrix.equals(new DefaultMatrix(new float[1])));
		assertEquals(false, matrix.equals(new DefaultMatrix(new float[ORDER * ORDER])));
	}
}
