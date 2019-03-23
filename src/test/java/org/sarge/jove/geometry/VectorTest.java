package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.util.MathsUtil;

public class VectorTest {
	private Vector vec;

	@BeforeEach
	public void before() {
		vec = new Vector(3, 4, 5);
	}

	@Test
	public void constructor() {
		assertEquals(3, vec.x, 0.0001f);
		assertEquals(4, vec.y, 0.0001f);
		assertEquals(5, vec.z, 0.0001f);
	}

	@Test
	public void axes() {
		assertEquals(new Vector(1, 0, 0), Vector.X_AXIS);
		assertEquals(new Vector(0, 1, 0), Vector.Y_AXIS);
		assertEquals(new Vector(0, 0, 1), Vector.Z_AXIS);
	}

	@Test
	public void converter() {
		final Vector result = Vector.CONVERTER.apply("3,4,5");
		assertEquals(vec, result);
	}

	@Test
	public void of() {
		final Point start = new Point(1, 2, 3);
		final Point end = new Point(4, 5, 6);
		assertEquals(new Vector(3, 3, 3), Vector.of(start, end));
	}

	@Test
	public void random() {
		vec = Vector.random();
		assertNotNull(vec);
		assertEquals(1f, vec.magnitude(), 0.0001f);
	}

	@Test
	public void angle() {
		assertEquals(0f, Vector.X_AXIS.angle(Vector.X_AXIS));
		// TODO
//		assertEquals(1f, Vector.X_AXIS.angle(Vector.Y_AXIS));
//		assertEquals(1f, Vector.X_AXIS.angle(new Vector(-1, 0, 0)));
	}

	@Test
	public void magnitude() {
		assertEquals(50, vec.magnitude(), 0.0001f);
	}

	@Test
	public void invert() {
		assertEquals(new Vector(-3, -4, -5), vec.invert());
	}

	@Test
	public void normalize() {
		final float scale = 1f / MathsUtil.sqrt(50);
		assertEquals(new Vector(3 * scale, 4 * scale, 5 *scale), vec.normalize());
	}

	@Test
	public void normalizeSelf() {
		final Vector result = vec.normalize();
		assertEquals(result, result.normalize());
	}

	@Test
	public void cross() {
		// TODO
	}

	@Test
	public void project() {
		final Tuple result = Vector.X_AXIS.project(vec);
		assertEquals(new Vector(3, 0, 0), result);
	}
}
