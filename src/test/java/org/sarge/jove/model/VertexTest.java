package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;

public class VertexTest {
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex();
	}

	@Test
	void constructor() {
		assertNotNull(vertex.components());
		assertEquals(0, vertex.components().count());
	}

	@Test
	void add() {
		vertex.add(Point.ORIGIN);
		vertex.add(Colour.WHITE);
		assertEquals(Point.ORIGIN, vertex.component(0));
		assertEquals(Colour.WHITE, vertex.component(1));
		assertArrayEquals(new Object[]{Point.ORIGIN, Colour.WHITE}, vertex.components().toArray());
	}

	@Test
	void componentInvalidIndex() {
		assertThrows(IndexOutOfBoundsException.class, () -> vertex.component(0));
	}

	@Nested
	class TransformTests {
		@BeforeEach
		void before() {
			vertex.add(Point.ORIGIN);
			vertex.add(Colour.WHITE);
		}

		@Test
		void transform() {
			vertex.transform(new int[]{1, 0});
			assertArrayEquals(new Object[]{Colour.WHITE, Point.ORIGIN}, vertex.components().toArray());
		}

		@Test
		void transformRemoveComponent() {
			vertex.transform(new int[]{1});
			assertArrayEquals(new Object[]{Colour.WHITE}, vertex.components().toArray());
		}

		@Test
		void transformInvalidIndex() {
			assertThrows(IndexOutOfBoundsException.class, () -> vertex.transform(new int[]{999}));
		}
	}

	@Test
	void buffer() {
		// Add vertex data
		vertex.add(new Point(1, 2, 3));

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
		vertex.add(Point.ORIGIN);
		assertEquals(vertex, vertex);
		assertEquals(vertex, Vertex.of(Point.ORIGIN));
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, new Vertex());
	}

	@Test
	void of() {
		vertex.add(Point.ORIGIN);
		assertEquals(vertex, Vertex.of(Point.ORIGIN));
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
