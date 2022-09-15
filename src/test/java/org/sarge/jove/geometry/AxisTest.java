package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Axis.*;
import static org.sarge.jove.util.MathsUtil.PI;

import org.junit.jupiter.api.*;

public class AxisTest {
	@Test
	void axes() {
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

	@Test
	void minimal() {
		assertEquals(X, Axis.minimal(new Vector(0, 1, 1)));
		assertEquals(Y, Axis.minimal(new Vector(1, 0, 1)));
		assertEquals(Z, Axis.minimal(new Vector(1, 1, 0)));
	}

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

	@Nested
	class ConverterTests {
		@Test
		void of() {
			assertEquals(X, Axis.of("X"));
			assertEquals(Y, Axis.of("Y"));
			assertEquals(Z, Axis.of("Z"));
		}

		@Test
		void axis() {
			assertEquals(X, Axis.CONVERTER.apply("X"));
			assertEquals(Y, Axis.CONVERTER.apply("Y"));
			assertEquals(Z, Axis.CONVERTER.apply("Z"));
		}

		@Test
		void invert() {
			assertEquals(X.invert(), Axis.CONVERTER.apply("-X"));
			assertEquals(Y.invert(), Axis.CONVERTER.apply("-Y"));
			assertEquals(Z.invert(), Axis.CONVERTER.apply("-Z"));
		}

		@Test
		void vector() {
			assertEquals(X, Axis.CONVERTER.apply("1 0 0"));
			assertEquals(Y, Axis.CONVERTER.apply("0 1 0"));
			assertEquals(Z, Axis.CONVERTER.apply("0 0 1"));
			assertEquals(new Vector(1, 2, 3), Axis.CONVERTER.apply("1 2 3"));
		}
	}
}
