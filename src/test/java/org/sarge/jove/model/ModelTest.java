package org.sarge.jove.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model.Builder;
import org.sarge.jove.model.Model.IndexedBuilder;

public class ModelTest {
	private static final Vertex.Layout LAYOUT = new Vertex.Layout(Vertex.Component.POSITION);

	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = Vertex.of(Point.ORIGIN);
	}

	@Nested
	class ModelTests {
		private Model model;

		@BeforeEach
		void before() {
			model = Model.of(Primitive.TRIANGLE_STRIP, LAYOUT, List.of(vertex, vertex, vertex));
		}

		@Test
		void constructor() {
			assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
			assertEquals(LAYOUT, model.layout());
			assertEquals(3, model.size());
			assertNotNull(model.index());
			assertEquals(false, model.index().isPresent());
		}

		@Test
		void vertices() {
			// Check VBO
			final int len = 3 * Point.SIZE * Float.BYTES;
			final Bufferable vertices = model.vertices();
			assertNotNull(vertices);
			assertEquals(len, vertices.length());

			// Buffer VBO
			final ByteBuffer buffer = ByteBuffer.allocate(len);
			vertices.buffer(buffer);
			assertEquals(len, buffer.capacity());
		}

		@Test
		void invalidNormals() {
			vertex = new Vertex.Builder().normal(Vector.X_AXIS).build();
			assertThrows(IllegalArgumentException.class, () -> Model.of(Primitive.LINES, new Vertex.Layout(Vertex.Component.NORMAL), List.of(vertex, vertex)));
		}

		@Test
		void invalidVertexCount() {
			assertThrows(IllegalArgumentException.class, () -> Model.of(Primitive.TRIANGLE_STRIP, LAYOUT, List.of(vertex)));
			assertThrows(IllegalArgumentException.class, () -> Model.of(Primitive.TRIANGLE_STRIP, LAYOUT, List.of(vertex, vertex)));
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
			// Build model
			final Model model = builder
					.primitive(Primitive.LINES)
					.layout(LAYOUT)
					.add(vertex)
					.add(vertex)
					.build();

			// Check model
			assertNotNull(model);
			assertEquals(Primitive.LINES, model.primitive());
			assertEquals(LAYOUT, model.layout());
			assertEquals(2, model.size());

			// Check index is empty
			assertNotNull(model.index());
			assertEquals(false, model.index().isPresent());
		}

		@Test
		void buildInvalidVertexCount() {
			builder.add(vertex);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void validate() {
			assertThrows(IllegalArgumentException.class, () -> builder.add(mock(Vertex.class)));
		}

		@Test
		void validateDisabled() {
			builder.validate(false).add(mock(Vertex.class));
		}

		@Test
		void index() {
			builder.add(vertex);
			assertThrows(UnsupportedOperationException.class, () -> builder.add(0));
			assertThrows(UnsupportedOperationException.class, () -> builder.indexOf(vertex));
		}
	}

	@Nested
	class IndexedBuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new IndexedBuilder().primitive(Primitive.LINES);
		}

		@Test
		void build() {
			// Create an indexed model
			final Model model = builder
					.add(vertex)
					.add(0)
					.build();

			// Check model
			assertNotNull(model);
			assertEquals(Primitive.LINES, model.primitive());
			assertEquals(LAYOUT, model.layout());
			assertEquals(2, model.size());
			assertNotNull(model.index());
			assertEquals(true, model.index().isPresent());

			// Check index buffer
			final int len = 2 * Integer.BYTES;
			final Bufferable index = model.index().get();
			assertNotNull(index);
			assertEquals(len, index.length());

			// Buffer index
			final ByteBuffer bb = ByteBuffer.allocate(len);
			index.buffer(bb);
			assertEquals(len, bb.capacity());
		}

		@Test
		void buildInvalidIndexCount() {
			builder.add(vertex);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void indexOf() {
			builder.add(vertex);
			assertEquals(0, builder.indexOf(vertex));
		}

		@Test
		void duplicate() {
			builder.add(vertex);
			builder.add(vertex);
			assertEquals(2, builder.build().size());
		}
	}
}
