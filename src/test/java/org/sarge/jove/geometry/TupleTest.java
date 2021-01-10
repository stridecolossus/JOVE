package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TupleTest {
	private Tuple tuple;

	@BeforeEach
	public void before() {
		tuple = new Tuple(1, 2, 3) {
			// Empty
		};
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
		tuple = new Tuple(new float[]{1, 2, 3}) {
			// Empty
		};
		assertEquals(1, tuple.x);
		assertEquals(2, tuple.y);
		assertEquals(3, tuple.z);
	}

	@Test
	public void copy() {
		tuple = new Tuple(tuple) {
			// Empty
		};
		assertEquals(1, tuple.x);
		assertEquals(2, tuple.y);
		assertEquals(3, tuple.z);
	}

	@Test
	public void dot() {
		assertEquals(1 * 1 + 2 * 2 + 3 * 3, tuple.dot(tuple));
	}

	@Test
	public void toArray() {
		final float[] array = tuple.toArray();
		assertEquals(3, array.length);
		assertEquals(1, array[0]);
		assertEquals(2, array[1]);
		assertEquals(3, array[2]);
	}

	@Test
	public void buffer() {
		final ByteBuffer buffer = ByteBuffer.allocate(3 * Float.BYTES);
		tuple.buffer(buffer);
		buffer.flip();
		assertEquals(tuple.x, buffer.getFloat());
		assertEquals(tuple.y, buffer.getFloat());
		assertEquals(tuple.z, buffer.getFloat());
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
