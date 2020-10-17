package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.Builder;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.model.Vertex.DefaultVertex;
import org.sarge.jove.model.Vertex.Layout;

public class VertexTest {
	private static final Component[] LAYOUT = {Component.POSITION, Component.NORMAL, Component.TEXTURE_COORDINATE, Component.COLOUR};

	private Vertex vertex;
	private Vector normal;

	@BeforeEach
	void before() {
		normal = new Vector(1, 2, 3);
		vertex = new DefaultVertex(Point.ORIGIN, normal, Coordinate2D.BOTTOM_RIGHT, Colour.WHITE);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, vertex.position());
		assertEquals(normal, vertex.normal());
		assertEquals(Coordinate2D.BOTTOM_RIGHT, vertex.coords());
		assertEquals(Colour.WHITE, vertex.colour());
	}

	@Test
	void of() {
		vertex = Vertex.of(Point.ORIGIN);
		assertEquals(Point.ORIGIN, vertex.position());
		assertEquals(null, vertex.normal());
		assertEquals(null, vertex.coords());
		assertEquals(null, vertex.colour());
	}

	@Nested
	class ComponentTests {
		@Test
		void size() {
			assertEquals(Point.SIZE, Component.POSITION.size());
			assertEquals(Vector.SIZE, Component.NORMAL.size());
			assertEquals(TextureCoordinate.Coordinate2D.SIZE, Component.TEXTURE_COORDINATE.size());
			assertEquals(Colour.SIZE, Component.COLOUR.size());
		}

		@Test
		void map() {
			assertEquals(Point.ORIGIN, Component.POSITION.map(vertex));
			assertEquals(normal, Component.NORMAL.map(vertex));
			assertEquals(Coordinate2D.BOTTOM_RIGHT, Component.TEXTURE_COORDINATE.map(vertex));
			assertEquals(Colour.WHITE, Component.COLOUR.map(vertex));
		}
	}

	@Nested
	class LayoutTests {
		private Layout layout;

		@BeforeEach
		void before() {
			layout = new Layout(LAYOUT);
		}

		@Test
		void constructor() {
			assertArrayEquals(LAYOUT, layout.components().toArray());
			assertEquals(3 + 3 + 2 + 4, layout.size());
		}

		@Test
		void constructorEmptyComponents() {
			assertThrows(IllegalArgumentException.class, () -> new Layout());
		}

		@Test
		void constructorDuplicateComponent() {
			assertThrows(IllegalArgumentException.class, () -> new Layout(Component.NORMAL, Component.NORMAL));
		}

		@Test
		void matches() {
			assertEquals(true, layout.matches(vertex));
			assertEquals(false, layout.matches(Vertex.of(Point.ORIGIN)));
		}

		@Test
		void buffer() {
			// Buffer vertex
			final int len = layout.size() * Float.BYTES;
			final ByteBuffer buffer = ByteBuffer.allocate(len);
			layout.buffer(vertex, buffer);
			assertEquals(len, buffer.limit());
			assertEquals(len, buffer.position());

			// Check buffer
			final ByteBuffer expected = ByteBuffer.allocate(len);
			vertex.position().buffer(expected);
			vertex.normal().buffer(expected);
			vertex.coords().buffer(expected);
			vertex.colour().buffer(expected);
			assertEquals(expected, buffer);
		}

		@Test
		void equals() {
			assertEquals(true, layout.equals(layout));
			assertEquals(true, layout.equals(new Layout(LAYOUT)));
			assertEquals(false, layout.equals(null));
			assertEquals(false, layout.equals(new Layout(Component.NORMAL)));
		}
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
		}

		@Test
		void build() {
			final Vertex result = builder
					.position(Point.ORIGIN)
					.normal(normal)
					.coords(Coordinate2D.BOTTOM_RIGHT)
					.colour(Colour.WHITE)
					.build();

			assertEquals(vertex, result);
		}
	}
}
