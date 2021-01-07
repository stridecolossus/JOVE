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
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model.Builder;
import org.sarge.jove.model.Model.DefaultModel;
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
			model = new DefaultModel(Model.DEFAULT_NAME, Primitive.TRIANGLE_STRIP, new Vertex.Layout(Vertex.Component.POSITION), List.of(vertex, vertex, vertex), null);
		}

		@Test
		void constructor() {
			assertEquals(Model.DEFAULT_NAME, model.name());
			assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
			assertEquals(LAYOUT, model.layout());
			assertEquals(3, model.count());
			assertNotNull(model.index());
			assertEquals(false, model.index().isPresent());
		}

		@Test
		void vertices() {
			final int len = 3 * Point.SIZE * Float.BYTES;
			final ByteBuffer vertices = model.vertices();
			assertNotNull(vertices);
			assertEquals(len, vertices.limit());
			assertEquals(0, vertices.position());
			// TODO - assertEquals(true, vertices.isReadOnly());
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
		void constructor() {
			assertEquals(0, builder.count());
			assertEquals(null, builder.index());
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
		void invalidNormals() {
			final Vertex normal = new Vertex.Builder().normal(Vector.X_AXIS).build();
			builder
					.primitive(Primitive.LINES)
					.layout(new Vertex.Layout(Vertex.Component.NORMAL))
					.add(normal)
					.add(normal);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void invalidVertexCount() {
			assertThrows(IllegalArgumentException.class, () -> builder.add(vertex).build());
		}

		@Test
		void build() {
			// Build model
			final Model model = builder
					.primitive(Primitive.LINES)
					.layout(Vertex.Component.POSITION)
					.add(vertex)
					.add(vertex)
					.build();

			// Check model
			assertNotNull(model);
			assertEquals(Primitive.LINES, model.primitive());
			assertEquals(LAYOUT, model.layout());
			assertEquals(2, model.count());

			// Check index is empty
			assertNotNull(model.index());
			assertEquals(false, model.index().isPresent());
		}
	}

	@Nested
	class IndexedBuilderTests {
		private IndexedBuilder builder;

		@BeforeEach
		void before() {
			builder = new IndexedBuilder();
		}

		@Test
		void constructor() {
			assertEquals(0, builder.count());
			assertNotNull(builder.index());
			assertEquals(List.of(), builder.index());
		}

		@Test
		void add() {
			builder.add(vertex);
			assertEquals(1, builder.count());
			assertEquals(List.of(0), builder.index());
		}

		@Test
		void addDuplicate() {
			builder.add(vertex);
			builder.add(vertex);
			assertEquals(1, builder.count());
			assertEquals(List.of(0, 0), builder.index());
			assertEquals(0, builder.indexOf(vertex));
		}

		@Test
		void indexOf() {
			builder.add(vertex);
			assertEquals(0, builder.indexOf(vertex));
		}

		@Test
		void indexOfUnknown() {
			assertThrows(IllegalArgumentException.class, () -> builder.indexOf(mock(Vertex.class)));
		}

		@Test
		void build() {
			// Init model
			builder.primitive(Primitive.LINES);

			// Build indexed model
			final Model model = builder
					.add(vertex)
					.add(vertex)
					.build();

			// Check model
			assertNotNull(model);
			assertEquals(Primitive.LINES, model.primitive());
			assertEquals(LAYOUT, model.layout());
			assertEquals(2, model.count());

			// Check index is present
			assertNotNull(model.index());
			assertEquals(true, model.index().isPresent());

			// Check index
			final ByteBuffer index = model.index().get();
			assertEquals(2 * Integer.BYTES, index.limit());
			assertEquals(0, index.position());
			assertEquals(true, index.isDirect());
			// TODO - assertEquals(true, index.isReadOnly());
		}
	}
}
