package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.geometry.Vector.X_AXIS;
import static org.sarge.jove.geometry.Vector.Y_AXIS;
import static org.sarge.jove.geometry.Vector.Z_AXIS;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Component.Layout;
import org.sarge.jove.util.MathsUtil;

class VectorTest {
	private Vector vec;

	@BeforeEach
	void before() {
		vec = new Vector(1, 2, 3);
	}

	@Test
	void constructor() {
		assertEquals(1, vec.x());
		assertEquals(2, vec.y());
		assertEquals(3, vec.z());
	}

	@Test
	void axes() {
		assertEquals(new Vector(1, 0, 0), X_AXIS);
		assertEquals(new Vector(0, 1, 0), Y_AXIS);
		assertEquals(new Vector(0, 0, 1), Z_AXIS);
	}

	@Test
	void array() {
		assertEquals(vec, Vector.of(new float[]{1, 2, 3}));
	}

	@Test
	void arrayInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> Vector.of(new float[]{1, 2}));
		assertThrows(IllegalArgumentException.class, () -> Vector.of(new float[]{1, 2, 3, 4}));
	}

	@Test
	void between() {
		assertEquals(vec, Vector.between(Point.ORIGIN, vec.toPoint()));
	}

	@Test
	void toPoint() {
		assertEquals(new Point(1, 2, 3), vec.toPoint());
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
	void normalize() {
		final float scale = 1 / (float) Math.sqrt(vec.magnitude());
		assertEquals(new Vector(1 * scale, 2 * scale, 3 * scale), vec.normalize());
	}

	@Test
	void normalizeSelf() {
		final Vector result = vec.normalize();
		assertSame(result, result.normalize());
	}

	@Test
	void dot() {
		assertEquals(1 * 1 + 2 * 2 + 3 * 3, vec.dot(vec));
	}

	@Test
	void dotAxes() {
		assertEquals(1f, X_AXIS.dot(X_AXIS));
		assertEquals(-1f, X_AXIS.dot(X_AXIS.invert()));
		assertEquals(0f, X_AXIS.dot(Y_AXIS));
		assertEquals(0f, X_AXIS.dot(Z_AXIS));
	}

	@Test
	void angle() {
		assertEquals(0f, X_AXIS.angle(X_AXIS));
		assertEquals(MathsUtil.PI, X_AXIS.angle(X_AXIS.invert()));
		assertEquals(MathsUtil.HALF_PI, X_AXIS.angle(Y_AXIS));
		assertEquals(MathsUtil.HALF_PI, X_AXIS.angle(Z_AXIS));
	}

	@Test
	void cross() {
		assertEquals(Z_AXIS, X_AXIS.cross(Y_AXIS));
		assertEquals(Z_AXIS.invert(), Y_AXIS.cross(X_AXIS));
	}

	@Test
	void project() {
		assertEquals(new Vector(1, 2, 3), vec.project(X_AXIS));
		assertEquals(new Vector(2, 4, 6), vec.project(Y_AXIS));
		assertEquals(new Vector(3, 6, 9), vec.project(Z_AXIS));
	}

	@Test
	void projectAxes() {
		assertEquals(new Vector(1, 0, 0), X_AXIS.project(vec));
		assertEquals(new Vector(0, 2, 0), Y_AXIS.project(vec));
		assertEquals(new Vector(0, 0, 3), Z_AXIS.project(vec));
	}

	@Test
	void reflect() {
		assertEquals(new Vector(-1, 2, 3), vec.reflect(X_AXIS));
		assertEquals(new Vector(1, -2, 3), vec.reflect(Y_AXIS));
		assertEquals(new Vector(1, 2, -3), vec.reflect(Z_AXIS));
	}

	@Test
	void reflectSelf() {
		assertEquals(vec.invert(), vec.reflect(vec.normalize()));
	}

	@Test
	void buffer() {
		final ByteBuffer buffer = ByteBuffer.allocate(3 * Float.BYTES);
		vec.buffer(buffer);
		buffer.flip();
		assertEquals(1, buffer.getFloat());
		assertEquals(2, buffer.getFloat());
		assertEquals(3, buffer.getFloat());
	}

	@Test
	void layout() {
		assertEquals(Layout.of(3, Float.class), vec.layout());
		assertEquals(3 * Float.BYTES, vec.length());
	}

	@Test
	public void equals() {
		assertEquals(true, vec.equals(vec));
		assertEquals(true, vec.equals(new Vector(1, 2, 3)));
		assertEquals(false, vec.equals(null));
		assertEquals(false, vec.equals(new Vector(4, 5, 6)));
	}
}
