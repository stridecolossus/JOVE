package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.Component;

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
		assertEquals(Vector.X, vertex.normal());
	}

	@Test
	void components() {
		assertNotNull(vertex.components());
		assertArrayEquals(new Object[]{Point.ORIGIN, Vector.X, Coordinate2D.BOTTOM_LEFT, Colour.BLACK}, vertex.components().toArray());
	}

	@Test
	void retain() {
		Vertex.retain(List.of(vertex), List.of(Component.NORMAL, Component.COLOUR));
		assertArrayEquals(new Object[]{Vector.X, Colour.BLACK}, vertex.components().toArray());
		assertEquals(Vector.X, vertex.normal());
		assertEquals((3 + 4) * Float.BYTES, vertex.length());
	}

	@Test
	void normals() {
		assertEquals(Layout.floats(3), Vertex.NORMALS);
	}

	@Test
	void layout() {
		assertEquals(List.of(Point.LAYOUT, Vertex.NORMALS, Coordinate2D.LAYOUT, Colour.LAYOUT), Vertex.LAYOUT);
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
