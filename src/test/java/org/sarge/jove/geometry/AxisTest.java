package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Axis.*;
import static org.sarge.jove.util.MathsUtil.PI;

import org.junit.jupiter.api.*;

public class AxisTest {
	@Test
	void vectors() {
		assertEquals(new Vector(1, 0, 0), X.vector());
		assertEquals(new Vector(0, 1, 0), Y.vector());
		assertEquals(new Vector(0, 0, 1), Z.vector());
	}

	@Test
	void invert() {
		assertEquals(new Vector(-1, 0, 0), X.vector().invert());
		assertEquals(new Vector(0, -1, 0), Y.vector().invert());
		assertEquals(new Vector(0, 0, -1), Z.vector().invert());
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
		@Test
		void x() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(1, 1, -1)
					.set(2, 2, -1)
					.build();

			assertEquals(expected, X.rotation(PI));
		}

		@Test
		void y() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(0, 0, -1)
					.set(2, 2, -1)
					.build();

			assertEquals(expected, Y.rotation(PI));
		}

		@Test
		void z() {
			final Matrix expected = new Matrix.Builder()
					.identity()
					.set(0, 0, -1)
					.set(1, 1, -1)
					.build();

			assertEquals(expected, Z.rotation(PI));
		}
	}

	@DisplayName("An axis can be parsed...")
	@Nested
	class ParseTests {
		@DisplayName("from a token representing that axis")
		@Test
		void axis() {
			assertEquals(X.vector(), Axis.parse("X"));
			assertEquals(Y.vector(), Axis.parse("Y"));
			assertEquals(Z.vector(), Axis.parse("Z"));
		}

		@DisplayName("from a token prefixed with the minus sign representing that inverted axis")
		@Test
		void invert() {
			assertEquals(X.vector().invert(), Axis.parse("-X"));
			assertEquals(Y.vector().invert(), Axis.parse("-Y"));
			assertEquals(Z.vector().invert(), Axis.parse("-Z"));
		}

		@DisplayName("from a vector tuple")
		@Test
		void vector() {
			assertEquals(X.vector(), Axis.parse("1 0 0"));
			assertEquals(Y.vector(), Axis.parse("0 1 0"));
			assertEquals(Z.vector(), Axis.parse("0 0 1"));
			assertEquals(new Vector(1, 2, 3), Axis.parse("1 2 3"));
		}
	}
}
