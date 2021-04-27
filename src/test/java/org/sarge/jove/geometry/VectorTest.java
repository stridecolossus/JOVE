package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Component.Layout;

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
		assertEquals(new Vector(1, 0, 0), Vector.X_AXIS);
		assertEquals(new Vector(0, 1, 0), Vector.Y_AXIS);
		assertEquals(new Vector(0, 0, 1), Vector.Z_AXIS);
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
	void dot() {
		assertEquals(1f, Vector.X_AXIS.dot(Vector.X_AXIS));
		assertEquals(0f, Vector.X_AXIS.dot(Vector.Y_AXIS));
	}

	@Test
	void angle() {
// TODO
//		assertEquals(1f, Vector.X_AXIS.angle(Vector.Y_AXIS));
//		assertEquals(1f, Vector.X_AXIS.angle(new Vector(-1, 0, 0)));
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
		assertEquals(vec.scale(scale), vec.normalize());
	}

	@Test
	void normalizeSelf() {
		final Vector result = vec.normalize();
		assertSame(result, result.normalize());
	}

	@Test
	void cross() {
		// TODO
	}

	@Test
	void project() {
		// TODO
		//assertEquals(new Vector(3, 0, 0), Vector.X_AXIS.project(vec));
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
