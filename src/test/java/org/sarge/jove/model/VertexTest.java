package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

class VertexTest {
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex(Point.ORIGIN);
	}

	@Test
	void position() {
		assertEquals(Point.ORIGIN, vertex.position());
	}

	@Test
	void normal() {
		assertThrows(UnsupportedOperationException.class, () -> vertex.add(Axis.X));
	}

	@Test
	void buffer() {
		final var buffer = ByteBuffer.allocate(4 * 3);
		vertex.buffer(buffer);
	}

	@Test
	void equals() {
		assertEquals(vertex, vertex);
		assertEquals(vertex, new Vertex(Point.ORIGIN));
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, new Vertex(new Point(1, 2, 3)));
	}
}
