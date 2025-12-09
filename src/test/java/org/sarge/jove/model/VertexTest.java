package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.model.Coordinate.Coordinate2D.TOP_RIGHT;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

class VertexTest {
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex(new Point(1, 2, 3), TOP_RIGHT);
	}

	@Test
	void empty() {
		assertThrows(NullPointerException.class, () -> new Vertex(Point.ORIGIN, null));
	}

	@Test
	void components() {
		assertEquals(List.of(new Point(1, 2, 3), TOP_RIGHT), vertex.components());
		assertEquals(new Point(1, 2, 3), vertex.component(0));
		assertEquals(TOP_RIGHT, vertex.component(1));
		assertThrows(IndexOutOfBoundsException.class, () -> vertex.component(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> vertex.component(2));
	}

	@Test
	void add() {
		vertex.add(Axis.Y);
		assertEquals(List.of(new Point(1, 2, 3), TOP_RIGHT, Axis.Y), vertex.components());
		assertEquals(Axis.Y, vertex.component(2));
	}

	@Test
	void remove() {
		vertex.remove(1);
		assertEquals(List.of(new Point(1, 2, 3)), vertex.components());
		assertThrows(IndexOutOfBoundsException.class, () -> vertex.remove(2));
	}

	@Test
	void buffer() {
		final int length = (3 + 2) * 4;
		final var buffer = ByteBuffer.allocate(length);
		vertex.buffer(buffer);
		assertEquals(0, buffer.remaining());
		buffer.rewind();
		assertEquals(1, buffer.getFloat());
		assertEquals(2, buffer.getFloat());
		assertEquals(3, buffer.getFloat());
		assertEquals(1, buffer.getFloat());
		assertEquals(0, buffer.getFloat());
	}

	@Test
	void equals() {
		assertEquals(vertex, vertex);
		assertEquals(vertex, new Vertex(new Point(1, 2, 3), TOP_RIGHT));
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, new Vertex(Point.ORIGIN));
	}
}
