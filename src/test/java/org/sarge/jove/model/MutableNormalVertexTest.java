package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

class MutableNormalVertexTest {
	private MutableNormalVertex vertex;

	@BeforeEach
	void before() {
		vertex = new MutableNormalVertex(Point.ORIGIN);
	}

	@Test
	void position() {
		assertEquals(Point.ORIGIN, vertex.position());
	}

	@Test
	void normal() {
		vertex.add(Axis.Y);
		assertEquals(Axis.Y, vertex.normal());
	}

	@Test
	void accumulated() {
		vertex.add(Axis.X);
		vertex.add(Axis.X.invert());
		vertex.add(Axis.Y);
		assertEquals(Axis.Y, vertex.normal());
	}

	@Test
	void buffer() {
		final var buffer = ByteBuffer.allocate(2 * 3 * 4);
		vertex.add(Axis.Y);
		vertex.buffer(buffer);
		assertEquals(2 * 3 * 4, buffer.position());
	}

	@Test
	void equals() {
		assertEquals(vertex, vertex);
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, new MutableNormalVertex(new Point(1, 2, 3)));
	}
}
