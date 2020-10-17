package org.sarge.jove.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.IndexedModel.IndexedBuilder;

public class IndexedModelTest {
	private static final Vertex.Layout LAYOUT = new Vertex.Layout(Vertex.Component.POSITION);

	private Vertex vertex;
	private ByteBuffer index;

	@BeforeEach
	void before() {
		vertex = Vertex.of(Point.ORIGIN);
		index = ByteBuffer.allocate(2 * Integer.BYTES).putInt(0).putInt(0).flip();
	}

	@Nested
	class IndexedModelTests {
		private IndexedModel model;

		@BeforeEach
		void before() {
			model = new IndexedModel(Primitive.LINES, new Vertex.Layout(Vertex.Component.POSITION), List.of(vertex), index, 2);
		}

		@Test
		void constructor() {
			assertEquals(Primitive.LINES, model.primitive());
			assertEquals(LAYOUT, model.layout());
			assertEquals(2, model.count());
			assertNotNull(model.index());
			assertEquals(Optional.of(index), model.index());
		}
	}

	@Nested
	class IndexedBuilderTests {
		private IndexedBuilder builder;

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
					.add(0)
					.build();

			// Check model
			assertNotNull(model);
			assertEquals(Primitive.LINES, model.primitive());
			assertEquals(LAYOUT, model.layout());
			assertEquals(2, model.count());
			assertNotNull(model.index());
			assertEquals(Optional.of(index), model.index());
		}

		@Test
		void buildInvalidIndexCount() {
			builder.add(vertex);
			builder.add(0);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void indexOf() {
			builder.add(vertex);
			assertEquals(0, builder.indexOf(vertex));
		}
	}

	@Nested
	class DuplicateIndexedBuilderTests {
		private IndexedBuilder builder;

		@BeforeEach
		void before() {
			builder = IndexedModel.duplicateIndexedBuilder().primitive(Primitive.LINE_STRIP);
		}

		@Test
		void duplicate() {
			builder.add(vertex);
			builder.add(vertex);
			assertEquals(2, builder.build().count());
		}
	}
}
