package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Matrix.Builder;

class MatrixTest {
	private static final int ORDER = 4;

	@Test
	void identity() {
		final Matrix identity = Matrix.identity(ORDER);
		assertNotNull(identity);
		assertEquals(ORDER, identity.order());
		assertEquals(identity, identity.matrix());
		for(int r = 0; r < ORDER; ++r) {
			for(int c = 0; c < ORDER; ++c) {
				assertEquals(r == c ? 1 : 0, identity.get(r, c));
			}
		}
	}

	@Test
	void index() {
		assertEquals(0, Matrix.index(0, 0, ORDER));
		assertEquals(4, Matrix.index(0, 1, ORDER));
		assertEquals(1, Matrix.index(1, 0, ORDER));
		assertEquals(5, Matrix.index(1, 1, ORDER));
		assertEquals(15, Matrix.index(3, 3, ORDER));
	}

	@Test
	void indexInvalid() {
		assertThrows(IllegalArgumentException.class, () -> Matrix.index(4, 0, ORDER));
	}

	@Nested
	class BuilderTests {
		private Builder builder;
		private float[] array;

		@BeforeEach
		void before() {
			builder = new Builder(ORDER);
			array = new float[ORDER * ORDER];
		}

		@Test
		void build() {
			final Matrix matrix = builder.build();
			assertNotNull(matrix);
			assertEquals(ORDER, matrix.order());
			assertNotNull(matrix.array());
			assertEquals(ORDER * ORDER, array.length);
		}

		@Test
		void empty() {
			assertArrayEquals(array, builder.build().array());
		}

		@Test
		void set() {
			builder.set(1, 2, 3);
			array[9] = 3;
			assertArrayEquals(array, builder.build().array());
		}

		@Test
		void row() {
			builder.row(1, new Vector(1, 2, 3));
			array[1] = 1;
			array[5] = 2;
			array[9] = 3;
			assertArrayEquals(array, builder.build().array());
		}

		@Test
		void column() {
			builder.column(1, new Vector(1, 2, 3));
			array[4] = 1;
			array[5] = 2;
			array[6] = 3;
			assertArrayEquals(array, builder.build().array());
		}

		@Test
		void invalidOrder() {
			assertThrows(IllegalArgumentException.class, () -> new Builder(0));
			assertThrows(IllegalArgumentException.class, () -> new Builder(-1));
		}
	}
}
