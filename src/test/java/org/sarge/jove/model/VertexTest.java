package org.sarge.jove.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.Builder;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.model.Vertex.DefaultVertex;
import org.sarge.jove.model.Vertex.Layout;
import org.sarge.jove.util.BufferFactory;

public class VertexTest {
	private Vertex vertex;
	private Vector normal;
	private TextureCoordinate coords;

	@BeforeEach
	void before() {
		normal = new Vector(1, 2, 3);
		coords = new TextureCoordinate.Coordinate2D(new float[]{4, 5});
		vertex = new DefaultVertex(Point.ORIGIN, normal, coords, Colour.WHITE);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, vertex.position());
		assertEquals(normal, vertex.normal());
		assertEquals(coords, vertex.coords());
		assertEquals(Colour.WHITE, vertex.colour());
	}

	@Test
	void constructorPosition() {
		vertex = new DefaultVertex(Point.ORIGIN);
		assertEquals(Point.ORIGIN, vertex.position());
		assertEquals(null, vertex.normal());
		assertEquals(null, vertex.coords());
		assertEquals(null, vertex.colour());
	}

	@Test
	void constructorMissingPosition() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultVertex(null, normal, coords, Colour.WHITE));
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
		void buffer() {
			// Build the expected result
			final int size = 3 + 3 + 2 + 4;
			final FloatBuffer expected = BufferFactory.floatBuffer(size);
			Point.ORIGIN.buffer(expected);
			normal.buffer(expected);
			coords.buffer(expected);
			Colour.WHITE.buffer(expected);

			// Buffer the vertex
			final FloatBuffer buffer = BufferFactory.floatBuffer(size);
			Component.POSITION.buffer(vertex, buffer);
			Component.NORMAL.buffer(vertex, buffer);
			Component.TEXTURE_COORDINATE.buffer(vertex, buffer);
			Component.COLOUR.buffer(vertex, buffer);

			// Compare buffers
			expected.flip();
			buffer.flip();
			assertTrue(buffer.equals(expected));
		}
	}

	private static final Component[] LAYOUT = {Component.POSITION, Component.NORMAL, Component.TEXTURE_COORDINATE, Component.COLOUR};

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
		void bufferVertex() {
			final FloatBuffer buffer = BufferFactory.byteBuffer(layout.size() * Float.BYTES).asFloatBuffer();
			layout.buffer(vertex, buffer);
			assertEquals(layout.size(), buffer.capacity());
			assertEquals(buffer.capacity(), buffer.position());
		}

		@Test
		void buffer() {
			final ByteBuffer buffer = layout.buffer(List.of(vertex, vertex));
			assertNotNull(buffer);
			assertEquals(2 * Float.BYTES * layout.size(), buffer.capacity());
			assertEquals(0, buffer.position());
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
			vertex = builder.position(Point.ORIGIN).build();
			assertEquals(new DefaultVertex(Point.ORIGIN), vertex);
		}

		@Test
		void buildMissingPosition() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
