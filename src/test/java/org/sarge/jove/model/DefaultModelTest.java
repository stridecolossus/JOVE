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
import org.sarge.jove.common.Component.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.DefaultModel.Builder;
import org.sarge.jove.model.DefaultModel.IndexedBuilder;
import org.sarge.jove.model.Model.Header;

class DefaultModelTest {
	private DefaultModel model;
	private Header header;
	private List<Integer> index;
	private Vertex vertex;

	@BeforeEach
	void before() {
		header = new Header(List.of(Layout.of(3)), Primitive.TRIANGLES, 3, true);
		vertex = Vertex.of(Point.ORIGIN);
		index = List.of(1, 1, 1);
		model = new DefaultModel(header, List.of(vertex), index);
	}

	@Test
	void constructor() {
		assertEquals(header, model.header());
		assertEquals(List.of(vertex), model.vertices());
		assertEquals(Optional.of(index), model.index());
	}

	@Nested
	class HeaderTests {
		@Test
		void header() {
			assertEquals(List.of(Layout.of(3)), header.layout());
			assertEquals(Primitive.TRIANGLES, header.primitive());
			assertEquals(3, header.count());
			assertEquals(true, header.clockwise());
		}

		@Test
		void invalidVertexCount() {
			assertThrows(IllegalArgumentException.class, () -> new Header(List.of(Layout.of(3)), Primitive.TRIANGLES, 4, false));
		}
	}

	@Test
	void unindexed() {
		model = new DefaultModel(header, List.of(vertex, vertex, vertex), null);
		assertEquals(Optional.empty(), model.index());
	}

	@Test
	void vertexBuffer() {
		final Bufferable vbo = model.vertexBuffer();
		assertNotNull(vbo);
		assertEquals(3 * 3 * Float.BYTES, vbo.length());
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
			final DefaultModel model = builder
					.primitive(Primitive.LINES)
					.clockwise(true)
					.add(vertex)
					.add(vertex)
					.build();

			// Verify model
			assertNotNull(model);
			assertEquals(new Header(List.of(Point.ORIGIN.layout()), Primitive.LINES, 2, true), model.header());
			assertEquals(false, model.isIndexed());
			assertEquals(List.of(vertex, vertex), model.vertices());
			assertEquals(Optional.empty(), model.index());
		}

		@Test
		void buildEmpty() {
			final DefaultModel model = builder.build();
			assertNotNull(model);
			assertEquals(new Header(List.of(), Primitive.TRIANGLE_STRIP, 0, false), model.header());
			assertEquals(List.of(), model.vertices());
			assertEquals(Optional.empty(), model.index());
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
			final DefaultModel model = builder
					.add(vertex)
					.add(other)
					.add(vertex)
					.build();

			// Verify the indexed model
			assertNotNull(model);
			assertEquals(true, model.isIndexed());
			assertEquals(List.of(vertex, other), model.vertices());
			assertEquals(Optional.of(List.of(0, 1, 0)), model.index());

			// Check model header
			final Header header = model.header();
			assertNotNull(header);
			assertEquals(3, header.count());
		}

		@Test
		void buildEmpty() {
			final DefaultModel model = builder.build();
			assertNotNull(model);
			assertEquals(0, model.header().count());
			assertEquals(List.of(), model.vertices());
			assertEquals(Optional.of(List.of()), model.index());
		}
	}
}
