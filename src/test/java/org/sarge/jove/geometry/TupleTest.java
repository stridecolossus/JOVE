package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.junit.jupiter.api.*;

public class TupleTest {
	private static class MockTuple extends Tuple {
		private MockTuple(float x, float y, float z) {
			super(x, y, z);
		}
	}

	private Tuple tuple;

	@BeforeEach
	void before() {
		tuple = new MockTuple(1, 2, 3);
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
		tuple = new Tuple(tuple) {
			// Empty
		};
		assertEquals(1, tuple.x);
		assertEquals(2, tuple.y);
		assertEquals(3, tuple.z);
	}

	private void create(float[] array) {
		tuple = new Tuple(array) {
			// Empty
		};
	}

	@DisplayName("A tuple can be created from an array")
	@Test
	void array() {
		create(new float[]{1, 2, 3});
		assertEquals(1, tuple.x);
		assertEquals(2, tuple.y);
		assertEquals(3, tuple.z);
	}

	@DisplayName("A tuple cannot be created from an array that does not comprise XYZ components")
	@Test
	void arrayInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> create(new float[0]));
		assertThrows(IllegalArgumentException.class, () -> create(new float[]{1, 2, 3, 4}));
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
		assertEquals(tuple, new MockTuple(1, 2, 3));
		assertNotEquals(tuple, null);
		assertNotEquals(tuple, new MockTuple(4, 5, 6));
	}
}
