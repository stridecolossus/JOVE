package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.MathsUtility;

class VectorTest {
	private Vector vector;

	@BeforeEach
	void before() {
		vector = new Vector(1, 2, 3);
	}

	@Test
	void constructor() {
		assertEquals(1, vector.x);
		assertEquals(2, vector.y);
		assertEquals(3, vector.z);
	}

	@DisplayName("A vector...")
	@Nested
	class VectorTests {
		@DisplayName("can be copied")
		@Test
		void copy() {
			assertEquals(vector, new Vector(vector));
		}

		@DisplayName("can be constructed from an array")
		@Test
		void array() {
			assertEquals(vector, new Vector(new float[]{1, 2, 3}));
		}

		@DisplayName("can be constructed between two points")
		@Test
		void between() {
			assertEquals(vector, Vector.between(new Point(1, 2, 3), new Point(2, 4, 6)));
		}

		@DisplayName("has a magnitude which is the squared length of the vector")
		@Test
		void magnitude() {
			assertEquals(1 * 1 + 2 * 2 + 3 * 3, vector.magnitude());
		}

		@DisplayName("can be inverted")
		@Test
		void invert() {
			assertEquals(new Vector(-1, -2, -3), vector.invert());
		}

		@DisplayName("can be composed")
		@Test
		void add() {
			assertEquals(new Vector(2, 4, 6), vector.add(vector));
		}

		@DisplayName("can be scaled")
		@Test
		void scalar() {
			assertEquals(new Vector(2, 4, 6), vector.multiply(2));
		}

		@DisplayName("can be normalized to the unit-vector in the same direction")
		@Test
		void normalize() {
			final float f = 1 / (float) Math.sqrt(vector.magnitude());
			final Vector expected = new Vector(1 * f, 2 * f, 3 * f);
			assertEquals(expected, vector.normalize());
		}
	}

	@DisplayName("The dot product...")
	@Nested
	class DotProduct {
		private Vector other;

		@BeforeEach
		void before() {
			other = new Vector(2, 3, 4);
		}

		@DisplayName("is the scalar product of two vectors")
		@Test
		void dot() {
			assertEquals(1 * 2 + 2 * 3 + 3 * 4, vector.dot(other));
		}

		@DisplayName("is commutative")
		@Test
		void commutative() {
			assertEquals(vector.dot(other), other.dot(vector));
		}

		@DisplayName("of a vector with itself is equivalent to the length squared of the vector")
		@Test
		void magntude() {
			assertEquals(vector.magnitude(), vector.dot(vector));
		}

		@DisplayName("of a unit-vector with itself is parallel")
		@Test
		void self() {
			final Normal unit = new Normal(vector);
			assertTrue(MathsUtility.isApproxEqual(1, unit.dot(unit)));
		}
	}

	@DisplayName("The angle between two vectors...")
	@Nested
	class AngleTests {
		@DisplayName("is zero for parallel vectors")
		@Test
		void parallel() {
			assertEquals(0, vector.angle(vector));
		}

		@DisplayName("is the maximum angle for opposite vectors")
		@Test
		void opposite() {
			assertEquals(MathsUtility.PI, vector.angle(vector.invert()));
		}

		@DisplayName("can be calculated for acute or obtuse angles")
		@Test
		void orthogonal() {
			assertEquals(MathsUtility.HALF_PI, X.angle(Y));
		}
	}

	@DisplayName("The cross product...")
	@Nested
	class CrossProductTests {
		private Vector unit, other, cross;

		@BeforeEach
		void other() {
			unit = new Normal(vector);
			other = new Normal(new Vector(4, 5, 6));
			cross = unit.cross(other);
		}

		@DisplayName("of two unit-vectors is a vector perpendicular to both")
		@Test
		void cross() {
			assertTrue(MathsUtility.isApproxZero(unit.dot(cross)));
		}

		@DisplayName("points in the opposite direction if the operation is reversed")
		@Test
		void inverse() {
			assertEquals(cross.invert(), other.cross(unit));
		}

		@DisplayName("of a vector with itself is undefined")
		@Test
		void self() {
			assertEquals(new Vector(0, 0, 0), vector.cross(vector));
			assertEquals(new Vector(0, 0, 0), unit.cross(unit));
		}
	}

	@DisplayName("The nearest point to a vector should be the intersection with the normal to that point")
	@Test
	void nearest() {
		final Point p = new Point(9, 8, 7);
		final Point nearest = vector.nearest(p);
		final Normal normal = Vector.between(p, nearest).normalize();
		assertTrue(MathsUtility.isApproxZero(vector.normalize().dot(normal)));
	}

	@DisplayName("A vector projected...")
	@Nested
	class ProjectionTests {
		@DisplayName("onto a cardinal axis extracts that component")
		@Test
		void cardinal() {
			assertEquals(new Vector(1, 0, 0), vector.project(X));
			assertEquals(new Vector(0, 2, 0), vector.project(Y));
			assertEquals(new Vector(0, 0, 3), vector.project(Z));
		}

		@DisplayName("onto itself is the same vector")
		@Test
		void self() {
			assertEquals(vector, vector.project(new Normal(vector)));
		}
	}

	@DisplayName("A vector reflected about...")
	@Nested
	class ReflectionTests {
		@DisplayName("the cardinal axes inverts that component")
		@Test
		void reflect() {
			assertEquals(new Vector(-1, 2, 3), vector.reflect(X));
			assertEquals(new Vector(1, -2, 3), vector.reflect(Y));
			assertEquals(new Vector(1, 2, -3), vector.reflect(Z));
		}

		@DisplayName("the inverse of a vector is the same")
		@Test
		void inverse() {
			assertEquals(new Vector(-1, 2, 3), vector.reflect(X.invert()));
			assertEquals(new Vector(1, -2, 3), vector.reflect(Y.invert()));
			assertEquals(new Vector(1, 2, -3), vector.reflect(Z.invert()));
		}

		@DisplayName("itself is the inverse of that vector")
		@Test
		void self() {
			assertEquals(vector.invert(), vector.reflect(new Normal(vector)));
		}
	}

	@Test
	public void equals() {
		assertEquals(vector, vector);
		assertEquals(vector, new Vector(1, 2, 3));
		assertNotEquals(vector, null);
		assertNotEquals(vector, new Vector(4, 5, 6));
	}
}
