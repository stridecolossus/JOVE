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
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.Component;

public class VertexTest {
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex(Point.ORIGIN, Vector.X, Coordinate2D.BOTTOM_LEFT, Colour.BLACK);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, vertex.position());
		assertEquals(Vector.X, vertex.normal());
		assertEquals(Coordinate2D.BOTTOM_LEFT, vertex.coordinate());
		assertEquals(Colour.BLACK, vertex.colour());
		assertEquals((3 + 3 + 2 + 4) * Float.BYTES, vertex.length());
	}

	@Test
	void stream() {
		assertNotNull(vertex.stream());
		assertArrayEquals(new Object[]{Point.ORIGIN, Vector.X, Coordinate2D.BOTTOM_LEFT, Colour.BLACK}, vertex.stream().toArray());
	}

	@Test
	void transform() {
		final Vertex expected = new Vertex(Point.ORIGIN, null, null, Colour.BLACK);
		assertEquals(expected, vertex.transform(List.of(Component.POSITION, Component.COLOUR)));
	}

	@Test
	void transformNullComponent() {
		assertThrows(IllegalArgumentException.class, () -> new Vertex(Point.ORIGIN).transform(Component.DEFAULT));
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
		assertEquals(vertex, new Vertex(Point.ORIGIN, Vector.X, Coordinate2D.BOTTOM_LEFT, Colour.BLACK));
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, new Vertex(Point.ORIGIN));
	}

	@Nested
	class ComponentTests {
		@Test
		void layout() {
			assertEquals(Point.LAYOUT, Component.POSITION.layout());
			assertEquals(Vector.LAYOUT, Component.NORMAL.layout());
			assertEquals(Coordinate2D.LAYOUT, Component.COORDINATE.layout());
			assertEquals(Colour.LAYOUT, Component.COLOUR.layout());
		}

		@Test
		void list() {
			assertEquals(List.of(Component.POSITION, Component.NORMAL, Component.COORDINATE, Component.COLOUR), Component.DEFAULT);
		}
	}

	@Nested
	class BuilderTests {
		private Vertex.Builder builder;

		@BeforeEach
		void before() {
			builder = new Vertex.Builder();
		}

		@Test
		void build() {
			builder
					.position(Point.ORIGIN)
					.normal(Vector.X)
					.coordinate(Coordinate2D.BOTTOM_LEFT)
					.colour(Colour.BLACK);

			assertEquals(vertex, builder.build());
		}

		@Test
		void buildMissingPosition() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
