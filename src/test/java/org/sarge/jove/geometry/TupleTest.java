package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.junit.jupiter.api.*;

class TupleTest {
	private Tuple tuple;

	@BeforeEach
	void before() {
		tuple = new Tuple(1, 2, 3);
	}

	@DisplayName("A tuple is comprised of XYZ components")
	@Test
	void constructor() {
		assertEquals(1, tuple.x);
		assertEquals(2, tuple.y);
		assertEquals(3, tuple.z);
	}

	@DisplayName("A tuple can be copied from an existing tuple")
	@Test
	void copy() {
		assertEquals(true, tuple.isEqual(new Tuple(tuple)));
	}

	@DisplayName("A tuple can be created from an array")
	@Test
	void array() {
		assertEquals(true, tuple.isEqual(new Tuple(new float[]{1, 2, 3})));
	}

	@DisplayName("A tuple cannot be created from an array that does not comprise XYZ components")
	@Test
	void arrayInvalidLength() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> new Tuple(new float[0]));
	}

	@DisplayName("A tuple can be converted to an array")
	@Test
	void toArray() {
		final float[] array = tuple.toArray();
		assertEquals(3, array.length);
		assertEquals(1, array[0]);
		assertEquals(2, array[1]);
		assertEquals(3, array[2]);
	}

	@DisplayName("A tuple can be written to an NIO buffer")
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

	@Test
	void equals() {
		assertTrue(tuple.isEqual(tuple));
		assertTrue(tuple.isEqual(new Tuple(1, 2, 3)));
		assertEquals(false, tuple.isEqual(new Tuple(4, 5, 6)));
	}

	@Test
	void string() {
		assertEquals("[1, 2, 3]", tuple.toString());
	}
}
