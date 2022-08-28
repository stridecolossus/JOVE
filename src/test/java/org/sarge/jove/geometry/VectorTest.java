package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Vector.*;

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

	@Test
	void copy() {
		assertEquals(vec, new Vector(vec));
	}

	@Test
	void array() {
		assertEquals(vec, new Vector(new float[]{1, 2, 3}));
	}

	@Test
	void axes() {
		assertEquals(new Vector(1, 0, 0), X);
		assertEquals(new Vector(0, 1, 0), Y);
		assertEquals(new Vector(0, 0, 1), Z);
	}

	@Test
	void between() {
		assertEquals(vec, Vector.between(new Point(1, 2, 3), new Point(2, 4, 6)));
	}

	@Test
	void magnitude() {
		assertEquals(1 * 1 + 2 * 2 + 3 * 3, vec.magnitude());
	}

	@Test
	void invert() {
		assertEquals(new Vector(-1, -2, -3), vec.invert());
	}

	@Test
	void add() {
		assertEquals(new Vector(2, 4, 6), vec.add(vec));
	}

	@Test
	void multiply() {
		assertEquals(new Vector(2, 4, 6), vec.multiply(2));
	}

	@Test
	void normalize() {
		final float f = 1 / (float) Math.sqrt(vec.magnitude());
		assertEquals(new Vector(1 * f, 2 * f, 3 * f), vec.normalize());
	}

	@Test
	void normalized() {
		final Vector result = vec.normalize();
		assertSame(result, result.normalize());
	}

	@Test
	void dot() {
		assertEquals(1f, X.dot(X));
		assertEquals(-1f, X.dot(X.invert()));
		assertEquals(0f, X.dot(Y));
		assertEquals(0f, X.dot(Z));
	}

	@Test
	void angle() {
		assertEquals(0f, X.angle(X));
		assertEquals(MathsUtil.PI, X.angle(X.invert()));
		assertEquals(MathsUtil.HALF_PI, X.angle(Y));
		assertEquals(MathsUtil.HALF_PI, X.angle(Z));
	}

	@Test
	void cross() {
		assertEquals(Z, X.cross(Y));
		assertEquals(Z.invert(), Y.cross(X));
	}

	@Test
	void project() {
		assertEquals(new Vector(1, 0, 0), vec.project(X));
		assertEquals(new Vector(0, 2, 0), vec.project(Y));
		assertEquals(new Vector(0, 0, 3), vec.project(Z));
	}

	@Test
	void projectSelf() {
		assertEquals(vec, vec.project(vec.normalize()));
	}

	@Test
	void reflect() {
		assertEquals(new Vector(-1, 2, 3), vec.reflect(X));
		assertEquals(new Vector(1, -2, 3), vec.reflect(Y));
		assertEquals(new Vector(1, 2, -3), vec.reflect(Z));
	}

	@Test
	void reflectInverse() {
		assertEquals(new Vector(-1, 2, 3), vec.reflect(X.invert()));
		assertEquals(new Vector(1, -2, 3), vec.reflect(Y.invert()));
		assertEquals(new Vector(1, 2, -3), vec.reflect(Z.invert()));
	}

	@Test
	void reflectSelf() {
		assertEquals(vec.invert(), vec.reflect(vec.normalize()));
	}

	@Test
	public void equals() {
		assertEquals(vec, vec);
		assertEquals(vec, new Vector(1, 2, 3));
		assertNotEquals(vec, null);
		assertNotEquals(vec, Vector.X);
	}
}
