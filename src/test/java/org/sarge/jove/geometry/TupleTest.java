package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TupleTest {
	private Tuple tuple;

	@BeforeEach
	void before() {
		tuple = new Tuple(1, 2, 3);
	}

	@Test
	void constructor() {
		assertEquals(1, tuple.x);
		assertEquals(2, tuple.y);
		assertEquals(3, tuple.z);
	}

	@Test
	void copy() {
		tuple = new Tuple(tuple);
		assertEquals(1, tuple.x);
		assertEquals(2, tuple.y);
		assertEquals(3, tuple.z);
	}

	@Test
	void array() {
		tuple = new Tuple(new float[]{1, 2, 3});
		assertEquals(1, tuple.x);
		assertEquals(2, tuple.y);
		assertEquals(3, tuple.z);
	}

	@Test
	void arrayInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> new Tuple(new float[0]));
		assertThrows(IllegalArgumentException.class, () -> new Tuple(new float[]{1, 2, 3, 4}));
	}

	@Test
	void get() {
		assertEquals(1, tuple.get(0));
		assertEquals(2, tuple.get(1));
		assertEquals(3, tuple.get(2));
	}

	@Test
	void getInvalidComponentIndex() {
		assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(3));
	}

	@Test
	void dot() {
		assertEquals(1 * 1 + 2 * 2 + 3 * 3, tuple.dot(tuple));
	}

	@Test
	void length() {
		assertEquals(3 * Float.BYTES, tuple.length());
	}

	@Test
	void buffer() {
		final ByteBuffer buffer = ByteBuffer.allocate(3 * Float.BYTES);
		tuple.buffer(buffer);
		buffer.flip();
		assertEquals(1, buffer.getFloat());
		assertEquals(2, buffer.getFloat());
		assertEquals(3, buffer.getFloat());
	}

	@Test
	void hash() {
		assertEquals(Objects.hash(1f, 2f, 3f), tuple.hashCode());
	}
}
