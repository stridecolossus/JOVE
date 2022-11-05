package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.MathsUtil;

class VectorTest {
	private Vector vec;

	@BeforeEach
	void before() {
		vec = new Vector(1, 2, 3);
	}

	@Test
	void constructor() {
		assertEquals(1, vec.x);
		assertEquals(2, vec.y);
		assertEquals(3, vec.z);
	}

	@DisplayName("A vector...")
	@Nested
	class VectorTests {
		@DisplayName("can be copied")
		@Test
		void copy() {
			assertEquals(vec, new Vector(vec));
		}

		@DisplayName("can be constructed from an array")
		@Test
		void array() {
			assertEquals(vec, new Vector(new float[]{1, 2, 3}));
		}

		@DisplayName("can be constructed between two points")
		@Test
		void between() {
			assertEquals(vec, Vector.between(new Point(1, 2, 3), new Point(2, 4, 6)));
		}

		@DisplayName("can be parsed from a delimited string")
		@Test
		void converter() {
			assertEquals(vec, Vector.CONVERTER.apply("1 2 3"));
			assertEquals(vec, Vector.CONVERTER.apply("1,2,3"));
		}

		@DisplayName("has a magnitude which is the squared length of the vector")
		@Test
		void magnitude() {
			assertEquals(1 * 1 + 2 * 2 + 3 * 3, vec.magnitude());
		}

		@DisplayName("can be inverted")
		@Test
		void invert() {
			assertEquals(new Vector(-1, -2, -3), vec.invert());
		}

		@DisplayName("can be composed")
		@Test
		void add() {
			assertEquals(new Vector(2, 4, 6), vec.add(vec));
		}

		@DisplayName("can be scaled")
		@Test
		void scalar() {
			assertEquals(new Vector(2, 4, 6), vec.multiply(2));
		}

		@DisplayName("can be multiplied component-wise")
		@Test
		void multiply() {
			assertEquals(new Vector(1, 4, 9), vec.multiply(vec));
			assertEquals(X, vec.multiply(X));
		}

		@DisplayName("can be normalized to the unit-vector in the same direction")
		@Test
		void normalize() {
			final float f = 1 / (float) Math.sqrt(vec.magnitude());
			final Vector expected = new Vector(1 * f, 2 * f, 3 * f);
			final Vector normal = vec.normalize();
			assertEquals(expected, normal);
			assertEquals(1, normal.magnitude());
		}
	}

	@DisplayName("The dot product...")
	@Nested
	class DotProduct {
		@DisplayName("of a vector with itself is equivalent to the length squared of the vector")
		@Test
		void dot() {
			assertEquals(vec.magnitude(), vec.dot(vec));
		}

		@DisplayName("of a unit-vector with itself is parallel")
		@Test
		void self() {
			final Vector unit = vec.normalize();
			assertTrue(MathsUtil.isEqual(1, unit.dot(unit)));
		}

		@DisplayName("is equivalent to the cosine of the angle between two vectors")
		@Test
		void angle() {
			assertEquals(MathsUtil.cos(MathsUtil.TWO_PI), vec.angle(vec));
			assertEquals(1, vec.angle(vec));
			assertEquals(-1, vec.angle(vec.invert()));
			assertEquals(MathsUtil.PI, X.angle(X.invert()));
			assertEquals(MathsUtil.HALF_PI, X.angle(Y));
			assertEquals(MathsUtil.HALF_PI, X.angle(Z));
		}
	}

	@DisplayName("The cross product...")
	@Nested
	class CrossProductTests {
		private Vector unit, other, cross;

		@BeforeEach
		void other() {
			unit = vec.normalize();
			other = new Vector(4, 5, 6).normalize();
			cross = unit.cross(other);
		}

		@DisplayName("of two unit-vectors is a vector perpendicular to both")
		@Test
		void cross() {
			assertTrue(MathsUtil.isZero(unit.dot(cross)));
		}

		@DisplayName("points in the opposite direction if the operation is reversed")
		@Test
		void inverse() {
			assertEquals(cross.invert(), other.cross(unit));
		}

		@DisplayName("of a vector with itself is undefined")
		@Test
		void self() {
			assertEquals(new Vector(0, 0, 0), vec.cross(vec));
			assertEquals(new Vector(0, 0, 0), unit.cross(unit));
		}
	}

	@DisplayName("The nearest point to a vector should be the intersection with the normal to that point")
	@Test
	void nearest() {
		final Point p = new Point(9, 8, 7);
		final Point nearest = vec.nearest(p);
		final Vector normal = Vector.between(p, nearest).normalize();
		assertTrue(MathsUtil.isZero(vec.normalize().dot(normal)));
	}

	@DisplayName("A vector projected...")
	@Nested
	class ProjectionTests {
		@DisplayName("onto a cardinal axis extracts that component")
		@Test
		void cardinal() {
			assertEquals(new Vector(1, 0, 0), vec.project(X));
			assertEquals(new Vector(0, 2, 0), vec.project(Y));
			assertEquals(new Vector(0, 0, 3), vec.project(Z));
		}

		@DisplayName("onto itself is the same vector")
		@Test
		void self() {
			assertEquals(vec, vec.project(vec));
		}
	}

	@DisplayName("A vector reflected about...")
	@Nested
	class ReflectionTests {
		@DisplayName("the cardinal axes inverts that component")
		@Test
		void reflect() {
			assertEquals(new Vector(-1, 2, 3), vec.reflect(X));
			assertEquals(new Vector(1, -2, 3), vec.reflect(Y));
			assertEquals(new Vector(1, 2, -3), vec.reflect(Z));
		}

		@DisplayName("the inverse of a vector is the same")
		@Test
		void inverse() {
			assertEquals(new Vector(-1, 2, 3), vec.reflect(X.invert()));
			assertEquals(new Vector(1, -2, 3), vec.reflect(Y.invert()));
			assertEquals(new Vector(1, 2, -3), vec.reflect(Z.invert()));
		}

		@DisplayName("itself is the inverse of that vector")
		@Test
		void self() {
			assertEquals(vec.invert(), vec.reflect(vec));
		}
	}

	@Test
	public void equals() {
		assertEquals(vec, vec);
		assertEquals(vec, new Vector(1, 2, 3));
		assertNotEquals(vec, null);
		assertNotEquals(vec, new Vector(4, 5, 6));
	}
}
