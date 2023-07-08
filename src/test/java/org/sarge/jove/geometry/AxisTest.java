package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Axis.*;
import static org.sarge.jove.util.Trigonometric.PI;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.Cosine;

class AxisTest {
	@Test
	void vectors() {
		assertEquals(new Vector(1, 0, 0), X);
		assertEquals(new Vector(0, 1, 0), Y);
		assertEquals(new Vector(0, 0, 1), Z);
	}

	@Test
	void invert() {
		assertEquals(new Vector(-1, 0, 0), X.invert());
		assertEquals(new Vector(0, -1, 0), Y.invert());
		assertEquals(new Vector(0, 0, -1), Z.invert());
	}

	// TODO - parameterized
	@Test
	void orthogonal() {
		assertEquals(1, X.dot(X));
		assertEquals(1, Y.dot(Y));
		assertEquals(1, Z.dot(Z));
	}

	@Test
	void dot() {
		assertEquals(0, X.dot(Y));
		assertEquals(0, X.dot(Z));
		assertEquals(0, Y.dot(Z));
	}

	// TODO - parameterized
	@Test
	void dot2() {
		final Vector vec = new Vector(3, 4, 5);
		final float expected = new Vector(1, 0, 0).dot(vec);
		assertEquals(expected, X.dot(vec));
		assertEquals(expected, vec.dot(X));
	}

	@Test
	void cross() {
		assertEquals(Z, X.cross(Y));
		assertEquals(Y, Z.cross(X));
		assertEquals(X, Y.cross(Z));
	}

	// TODO - parameterized
	@Test
	void cross2() {
		final Vector vec = new Vector(3, 4, 5);
		final Vector expected = new Vector(1, 0, 0).cross(vec);
		assertEquals(expected, X.cross(vec));
	}

	@DisplayName("The axis corresponding to the minimal component of a vector can be determined")
	@Test
	void minimal() {
		assertEquals(X, Axis.minimal(new Vector(0, 1, 1)));
		assertEquals(Y, Axis.minimal(new Vector(1, 0, 1)));
		assertEquals(Z, Axis.minimal(new Vector(1, 1, 0)));
	}

	@DisplayName("A rotation matrix about an axis can be constructed")
	@Nested
	class RotationTests {
		private Matrix.Builder expected;

		@BeforeEach
		void before() {
			expected = new Matrix.Builder().identity();
		}

		@Test
		void x() {
			expected
					.set(1, 1, -1)
					.set(2, 2, -1);

			assertEquals(expected.build(), X.rotation(PI, Cosine.DEFAULT));
		}

		@Test
		void y() {
			expected
					.set(0, 0, -1)
					.set(2, 2, -1);

			assertEquals(expected.build(), Y.rotation(PI, Cosine.DEFAULT));
		}

		@Test
		void z() {
			expected
					.set(0, 0, -1)
					.set(1, 1, -1);

			assertEquals(expected.build(), Z.rotation(PI, Cosine.DEFAULT));
		}
	}

	@DisplayName("An axis can be parsed...")
	@Nested
	class ParseTests {
		@DisplayName("from a token representing that axis")
		@Test
		void axis() {
			assertEquals(X, Axis.parse("X"));
			assertEquals(Y, Axis.parse("Y"));
			assertEquals(Z, Axis.parse("Z"));
		}

		@DisplayName("from a token prefixed with the minus sign representing that axis inverted")
		@Test
		void invert() {
			assertEquals(X.invert(), Axis.parse("-X"));
			assertEquals(Y.invert(), Axis.parse("-Y"));
			assertEquals(Z.invert(), Axis.parse("-Z"));
		}

		@DisplayName("from a vector tuple")
		@Test
		void vector() {
			assertEquals(X, Axis.parse("1 0 0"));
			assertEquals(Y, Axis.parse("0 1 0"));
			assertEquals(Z, Axis.parse("0 0 1"));
			assertEquals(new Vector(1, 2, 3), Axis.parse("1 2 3"));
		}
	}
}
