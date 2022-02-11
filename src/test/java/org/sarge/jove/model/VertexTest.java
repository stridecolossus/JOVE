package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.BufferHelper;

public class VertexTest {
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex();
	}

	@Test
	void constructor() {
		assertEquals(null, vertex.position());
		assertEquals(null, vertex.normal());
		assertEquals(null, vertex.coordinate());
		assertEquals(null, vertex.colour());
	}

	@Test
	void position() {
		vertex.position(Point.ORIGIN);
		assertEquals(Point.ORIGIN, vertex.position());
	}

	@Test
	void normal() {
		vertex.normal(Vector.X);
		assertEquals(Vector.X, vertex.normal());
	}

	@Test
	void coordinate() {
		vertex.coordinate(Coordinate2D.BOTTOM_LEFT);
		assertEquals(Coordinate2D.BOTTOM_LEFT, vertex.coordinate());
	}

	@Test
	void colour() {
		vertex.colour(Colour.WHITE);
		assertEquals(Colour.WHITE, vertex.colour());
	}

	@Test
	void simple() {
		vertex.position(Point.ORIGIN);
		assertEquals(vertex, Vertex.of(Point.ORIGIN));
	}

	@Test
	void buffer() {
		// Add some vertex components
		vertex.position(Point.ORIGIN);
		vertex.coordinate(Coordinate2D.BOTTOM_LEFT);

		// Buffer vertex
		final int len = (3 + 2) * Float.BYTES;
		final ByteBuffer bb = BufferHelper.allocate(len);
		vertex.buffer(bb);
		assertEquals(0, bb.remaining());

		// Check buffer
		final ByteBuffer expected = BufferHelper.allocate(len);
		Point.ORIGIN.buffer(expected);
		Coordinate2D.BOTTOM_LEFT.buffer(expected);
		assertEquals(expected, bb);
	}

	@Test
	void equals() {
		assertEquals(vertex, vertex);
		assertEquals(vertex, new Vertex());
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, new Vertex().position(Point.ORIGIN));
	}

	@Test
	void equalsComponents() {
		vertex.position(Point.ORIGIN);
		vertex.normal(Vector.X);
		vertex.coordinate(Coordinate2D.BOTTOM_LEFT);
		vertex.colour(Colour.WHITE);
		assertEquals(vertex, vertex);
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, new Vertex());
	}
}
