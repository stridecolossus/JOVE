package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Axis.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.MathsUtility;

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

	@Test
	void cross() {
		assertEquals(Z, X.cross(Y));
		assertEquals(Y, Z.cross(X));
		assertEquals(X, Y.cross(Z));
	}

	@Nested
	class RotationTests {
		private Angle angle;
		private Matrix.Builder matrix;

		@BeforeEach
		void before() {
			angle = new Angle(MathsUtility.PI);
			matrix = new Matrix.Builder(4).identity();
		}

    	@Test
    	void x() {
    		matrix.set(1, 1, -1);
    		matrix.set(2, 2, -1);
    		assertEquals(matrix.build(), X.rotation(angle));
    	}

    	@Test
    	void y() {
    		matrix.set(0, 0, -1);
    		matrix.set(2, 2, -1);
    		assertEquals(matrix.build(), Y.rotation(angle));
    	}

    	@Test
    	void z() {
    		matrix.set(0, 0, -1);
    		matrix.set(1, 1, -1);
    		assertEquals(matrix.build(), Z.rotation(angle));
    	}
	}

	@DisplayName("The axis corresponding to the minimal component of a vector can be determined")
	@Test
	void minimal() {
		assertEquals(X, Axis.minimal(new Vector(0, 1, 1)));
		assertEquals(Y, Axis.minimal(new Vector(1, 0, 1)));
		assertEquals(Z, Axis.minimal(new Vector(1, 1, 0)));
	}

	@DisplayName("An axis can be parsed")
	@Test
	void parse() {
		assertEquals(X, Axis.parse('X'));
		assertEquals(Y, Axis.parse('Y'));
		assertEquals(Z, Axis.parse('Z'));
	}
}
