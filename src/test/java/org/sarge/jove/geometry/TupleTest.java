package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.junit.jupiter.api.*;

public class TupleTest {
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
		assertEquals(tuple, new Tuple(tuple));
	}

	@DisplayName("A tuple can be created from an array")
	@Test
	void array() {
		assertEquals(tuple, new Tuple(new float[]{1, 2, 3}));
	}

	@DisplayName("A tuple cannot be created from an array that does not comprise XYZ components")
	@Test
	void arrayInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> new Tuple(new float[0]));
		assertThrows(IllegalArgumentException.class, () -> new Tuple(new float[]{1, 2, 3, 4}));
	}

	@DisplayName("The components of a tuple can be retrieved by index")
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

	@DisplayName("The dot product of a tuple is equivalent to the magnitude of a vector")
	@Test
	void dot() {
		assertEquals(1 * 1 + 2 * 2 + 3 * 3, tuple.dot(tuple));
	}

	@DisplayName("The minimum component can be extracted from the tuple")
	@Test
	void min() {
		assertEquals(1, tuple.min());
	}

	@DisplayName("The maximum component can be extracted from the tuple")
	@Test
	void max() {
		assertEquals(3, tuple.max());
	}

	@DisplayName("The minimum components of two tuples can be calculated")
	@Test
	void minimum() {
		assertEquals(tuple, Tuple.min(tuple, tuple));
		assertEquals(tuple, Tuple.min(tuple, new Tuple(4, 5, 6)));
	}

	@DisplayName("The maximum components of two tuples can be calculated")
	@Test
	void maximum() {
		assertEquals(tuple, Tuple.max(tuple, tuple));
		assertEquals(tuple, Tuple.max(tuple, new Tuple(0, 0, 0)));
	}

	@DisplayName("A tuple has a length in bytes")
	@Test
	void length() {
		assertEquals(3 * Float.BYTES, tuple.length());
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
		assertEquals(tuple, tuple);
		assertEquals(tuple, new Tuple(1, 2, 3));
		assertNotEquals(tuple, null);
		assertNotEquals(tuple, new Tuple(4, 5, 6));
	}
}
