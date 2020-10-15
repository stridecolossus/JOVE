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
import org.sarge.jove.model.Model.Builder;

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
			assertEquals(3 * Point.SIZE * Float.BYTES, model.length());
		}

		@Test
		void buffer() {
			final ByteBuffer buffer = ByteBuffer.allocate((int) model.length());
			model.buffer(buffer);
			assertEquals(3 * Point.SIZE * Float.BYTES, buffer.capacity());
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
			final Model model = builder
					.primitive(Primitive.LINE_LIST)
					.layout(LAYOUT)
					.add(vertex)
					.add(vertex)
					.build();

			assertNotNull(model);
			assertEquals(Primitive.LINE_LIST, model.primitive());
			assertEquals(LAYOUT, model.layout());
			assertEquals(2, model.size());
		}

		@Test
		void buildPrimitiveMismatch() {
			builder.add(vertex);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void invalidConfiguration() {
			builder.add(vertex);
			assertThrows(IllegalStateException.class, () -> builder.primitive(Primitive.LINE_LIST));
			assertThrows(IllegalStateException.class, () -> builder.layout(new Vertex.Layout(Vertex.Component.COLOUR)));
		}

		@Test
		void invalidVertex() {
			assertThrows(IllegalArgumentException.class, () -> builder.add(mock(Vertex.class)));
		}
	}
}
