package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;

public class VertexTest {
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex(Point.ORIGIN, Colour.WHITE);
	}

	@Test
	void components() {
		assertEquals(Point.ORIGIN, vertex.component(0));
		assertEquals(Colour.WHITE, vertex.component(1));
	}

	@Test
	void stream() {
		assertNotNull(vertex.components());
		assertArrayEquals(new Object[]{Point.ORIGIN, Colour.WHITE}, vertex.components().toArray());
	}

	@Test
	void componentInvalidIndex() {
		assertThrows(IndexOutOfBoundsException.class, () -> vertex.component(999));
	}

	@DisplayName("Vertex can be re-ordered")
	@Test
	void transform() {
		vertex.transform(new int[]{1, 0});
		assertArrayEquals(new Object[]{Colour.WHITE, Point.ORIGIN}, vertex.components().toArray());
	}

	@DisplayName("Vertex components can be removed")
	@Test
	void transformRemoveComponent() {
		vertex.transform(new int[]{1});
		assertArrayEquals(new Object[]{Colour.WHITE}, vertex.components().toArray());
	}

	@DisplayName("Cannot apply a transform with an invalid index")
	@Test
	void transformInvalidIndex() {
		assertThrows(IndexOutOfBoundsException.class, () -> vertex.transform(new int[]{999}));
	}

	@DisplayName("Vertex can be written to an NIO buffer")
	@Test
	void buffer() {
		// Add vertex data
		vertex = new Vertex(new Point(1, 2, 3));

		// Buffer vertex
		final ByteBuffer bb = ByteBuffer.allocate(3 * Float.BYTES);
		vertex.buffer(bb);
		assertEquals(0, bb.remaining());

		// Check buffered data
		bb.flip();
		assertEquals(1, bb.getFloat());
		assertEquals(2, bb.getFloat());
		assertEquals(3, bb.getFloat());
	}

	@Test
	void equals() {
		assertEquals(vertex, vertex);
		assertEquals(vertex, new Vertex(Point.ORIGIN, Colour.WHITE));
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, new Vertex());
	}

	@Test
	void layout() {
		assertEquals(List.of(Point.LAYOUT, Vertex.NORMALS, Coordinate2D.LAYOUT, Colour.LAYOUT), Vertex.LAYOUT);
	}

	@Test
	void normals() {
		assertEquals(Layout.floats(3), Vertex.NORMALS);
	}
}
