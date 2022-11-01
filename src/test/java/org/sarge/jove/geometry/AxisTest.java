package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Axis.*;
import static org.sarge.jove.util.MathsUtil.PI;

import org.junit.jupiter.api.*;

public class AxisTest {
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
