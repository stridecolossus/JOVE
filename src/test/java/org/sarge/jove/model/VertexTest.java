package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class VertexTest {
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex()
				.position(Point.ORIGIN)
				.normal(Vector.X)
				.coordinate(Coordinate2D.BOTTOM_LEFT)
				.colour(Colour.BLACK);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, vertex.position());
		assertEquals(Vector.X, vertex.normal());
		assertEquals(Coordinate2D.BOTTOM_LEFT, vertex.coordinate());
		assertEquals(Colour.BLACK, vertex.colour());
	}

	@Test
	void components() {
		assertNotNull(vertex.components());
		assertEquals(List.of(Point.ORIGIN, Vector.X, Coordinate2D.BOTTOM_LEFT, Colour.BLACK), vertex.components());
	}

	@Test
	void length() {
		assertEquals((3 + 3 + 2 + 4) * Float.BYTES, vertex.length());
	}

	@Test
	void buffer() {
		// Buffer vertex
		final ByteBuffer bb = ByteBuffer.allocate(vertex.length());
		vertex.buffer(bb);
		assertEquals(0, bb.remaining());

		// Check buffered data
		final ByteBuffer expected = ByteBuffer.allocate(vertex.length());
		expected.putFloat(0).putFloat(0).putFloat(0);
		expected.putFloat(1).putFloat(0).putFloat(0);
		expected.putFloat(0).putFloat(1);
		expected.putFloat(0).putFloat(0).putFloat(0).putFloat(1);
		assertEquals(expected.flip(), bb.flip());
	}

	@Test
	void equals() {
		assertEquals(vertex, vertex);
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, new Vertex().position(Point.ORIGIN));
	}
}
