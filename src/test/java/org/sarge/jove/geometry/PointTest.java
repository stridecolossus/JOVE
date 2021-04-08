package org.sarge.jove.geometry;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.util.MathsUtil;

@SuppressWarnings("static-method")
public class PointTest {
	private Point pos;

	@BeforeEach
	void before() {
		pos = new Point(1, 2, 3);
	}

	@Test
	void constructor() {
		assertEquals(1, pos.x());
		assertEquals(2, pos.y());
		assertEquals(3, pos.z());
	}

	@Test
	void origin() {
		assertEquals(new Point(0, 0, 0), Point.ORIGIN);
	}

	@Test
	void array() {
		assertEquals(pos, Point.of(new float[]{1, 2, 3}));
	}

	@Test
	void arrayInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> Point.of(new float[]{1, 2}));
		assertThrows(IllegalArgumentException.class, () -> Point.of(new float[]{1, 2, 3, 4}));
	}

	@Test
	void toVector() {
		assertEquals(new Vector(1, 2, 3), pos.toVector());
	}

	@Test
	void distance() {
		assertTrue(MathsUtil.isEqual(27, pos.distance(new Point(4, 5, 6))));
	}

	@Test
	void add() {
		assertEquals(new Point(5, 7, 9), pos.add(new Vector(4, 5, 6)));
	}

	@Test
	void buffer() {
		final ByteBuffer buffer = ByteBuffer.allocate(3 * Float.BYTES);
		pos.buffer(buffer);
		buffer.flip();
		assertEquals(1, buffer.getFloat());
		assertEquals(2, buffer.getFloat());
		assertEquals(3, buffer.getFloat());
	}

	@Test
	void length() {
		assertEquals(3 * Float.BYTES, pos.length());
	}

	@Test
	public void equals() {
		assertEquals(true, pos.equals(pos));
		assertEquals(true, pos.equals(new Point(1, 2, 3)));
		assertEquals(false, pos.equals(null));
		assertEquals(false, pos.equals(Point.ORIGIN));
	}
}
