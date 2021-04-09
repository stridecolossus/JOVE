package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.Header;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.model.Vertex.Layout;
import org.sarge.jove.model.VertexModel.Builder;
import org.sarge.jove.model.VertexModel.IndexedBuilder;

public class VertexModelTest {
	private VertexModel model;
	private Header header;
	private Layout layout;
	private List<Integer> index;
	private Vertex vertex;

	@BeforeEach
	void before() {
		header = new Header(Primitive.TRIANGLES, true, 3);
		layout = new Layout(Component.POSITION);
		vertex = Vertex.of(Point.ORIGIN);
		index = List.of(1, 1, 1);
		model = new VertexModel(header, layout, List.of(vertex), index);
	}

	@Test
	void constructor() {
		assertEquals(header, model.header());
		assertEquals(List.of(vertex), model.vertices());
		assertEquals(Optional.of(index), model.index());
	}

	@Test
	void unindexed() {
		model = new VertexModel(new Header(Primitive.POINTS, true, 1), layout, List.of(vertex), null);
		assertEquals(Optional.empty(), model.index());
	}

	@Test
	void vertexBuffer() {
		final Bufferable vbo = model.vertexBuffer();
		assertNotNull(vbo);
		assertEquals(3 * Float.BYTES, vbo.length());
	}

	@Test
	void indexBuffer() {
		final Bufferable indices = model.indexBuffer().orElseThrow();
		assertNotNull(indices);
		assertEquals(3 * Integer.BYTES, indices.length());
	}

	@Test
	void buffer() {
		final BufferedModel buffered = model.buffer();
		assertNotNull(buffered);
		assertEquals(header, buffered.header());
		// TODO - compare buffers (and in above tests)
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
			final VertexModel model = builder
					.primitive(Primitive.LINES)
					.clockwise(true)
					.layout(layout)
					.add(vertex)
					.add(vertex)
					.build();

			// Verify model
			assertNotNull(model);
			assertEquals(new Header(Primitive.LINES, true, 2), model.header());
			assertEquals(false, model.isIndexed());
			assertEquals(List.of(vertex, vertex), model.vertices());
			assertEquals(Optional.empty(), model.index());
		}

		@Test
		void buildEmpty() {
			final VertexModel model = builder.build();
			assertNotNull(model);
			assertEquals(new Header(Primitive.TRIANGLE_STRIP, false, 0), model.header());
			assertEquals(List.of(), model.vertices());
			assertEquals(Optional.empty(), model.index());
		}

		@Test
		void addInvalidLayout() {
			builder.layout(new Layout(Component.POSITION, Component.COLOUR));
			assertThrows(IllegalArgumentException.class, () -> builder.add(vertex));
		}

		@Test
		void buildInvalidVertexCount() {
			builder.add(vertex);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
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
		void build() {
			// Build an indexed model that re-uses some vertices
			final Vertex other = Vertex.of(new Point(1, 2, 3));
			final VertexModel model = builder
					.add(vertex)
					.add(other)
					.add(vertex)
					.build();

			// Verify the indexed model
			assertNotNull(model);
			assertEquals(true, model.isIndexed());
			assertEquals(new Header(Primitive.TRIANGLE_STRIP, false, 3), model.header());
			assertEquals(List.of(vertex, other), model.vertices());
			assertEquals(Optional.of(List.of(0, 1, 0)), model.index());
		}

		@Test
		void buildEmpty() {
			final VertexModel model = builder.build();
			assertNotNull(model);
			assertEquals(new Header(Primitive.TRIANGLE_STRIP, false, 0), model.header());
			assertEquals(List.of(), model.vertices());
			assertEquals(Optional.of(List.of()), model.index());
		}
	}
}
