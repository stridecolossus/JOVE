package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Tuple.Swizzle;

public class TupleTest {
	private Tuple tuple;

	@BeforeEach
	public void before() {
		tuple = new Tuple(1, 2, 3);
	}

	@Test
	public void constructor() {
		assertEquals(1, tuple.x);
		assertEquals(2, tuple.y);
		assertEquals(3, tuple.z);
		assertEquals(3, Tuple.SIZE);
		assertEquals(3 * Float.BYTES, tuple.length());
	}

	@Test
	public void array() {
		final Tuple other = new Tuple(new float[]{1, 2, 3});
		assertEquals(tuple, other);
	}

	@Test
	public void copy() {
		assertEquals(tuple, new Tuple(tuple));
	}

	@Test
	public void dot() {
		assertFloatEquals(1 * 1 + 2 * 2 + 3 * 3, tuple.dot(tuple));
	}

	@Test
	public void swizzle() {
		assertEquals(new Tuple(2, 1, 3), Swizzle.XY.apply(tuple));
		assertEquals(new Tuple(3, 2, 1), Swizzle.XZ.apply(tuple));
		assertEquals(new Tuple(1, 3, 2), Swizzle.YZ.apply(tuple));
	}

	@Test
	public void toArray() {
		final float[] array = tuple.toArray();
		assertEquals(3, array.length);
		assertFloatEquals(1, array[0]);
		assertFloatEquals(2, array[1]);
		assertFloatEquals(3, array[2]);
	}

	@Test
	public void buffer() {
		final ByteBuffer buffer = ByteBuffer.allocate(3 * Float.BYTES);
		tuple.buffer(buffer);
		buffer.flip();
		assertFloatEquals(tuple.x, buffer.getFloat());
		assertFloatEquals(tuple.y, buffer.getFloat());
		assertFloatEquals(tuple.z, buffer.getFloat());
	}

	@Test
	public void equals() {
		assertTrue(tuple.equals(tuple));
		assertFalse(tuple.equals(null));
		assertFalse(tuple.equals(Point.ORIGIN));
		assertFalse(tuple.equals(new Point(1, 2, 3)));
	}

	@Test
	public void hash() {
		// TODO
	}
}
