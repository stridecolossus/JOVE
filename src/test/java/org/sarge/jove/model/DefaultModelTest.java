package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;

class DefaultModelTest {
	private DefaultModel model;
	private Bufferable vertices;

	@BeforeEach
	void before() {
		vertices = mock(Bufferable.class);
		model = new DefaultModel(Primitive.TRIANGLE_STRIP, 3, CompoundLayout.of(Point.LAYOUT), vertices, null);
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(3, model.count());
		assertEquals(CompoundLayout.of(Point.LAYOUT), model.layout());
		assertEquals(vertices, model.vertices());
		assertEquals(false, model.isIndexed());
		assertEquals(Optional.empty(), model.index());
	}

	@DisplayName("A model cannot contain normals if unsupported by the drawing primitive")
	@Test
	void normals() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultModel(Primitive.LINES, 2, CompoundLayout.of(Point.LAYOUT, Model.NORMALS), vertices, null));
	}

	@DisplayName("An indexed model also contains an index buffer")
	@Test
	void indexed() {
		final Model model = new DefaultModel(Primitive.TRIANGLE_STRIP, 3, CompoundLayout.of(Point.LAYOUT), vertices, vertices);
		assertEquals(true, model.isIndexed());
		assertEquals(Optional.of(vertices), model.index());
	}

	@Nested
	class BuilderTests {
		private DefaultModel.Builder builder;
		private Vertex vertex;

		@BeforeEach
		void before() {
			builder = new DefaultModel.Builder();
			vertex = Vertex.of(Point.ORIGIN);
		}

		@Test
		void build() {
			// Build unindexed model
			final Model model = builder
					.primitive(Primitive.TRIANGLE_STRIP)
					.layout(Point.LAYOUT)
					.add(vertex)
					.add(vertex)
					.add(vertex)
					.build();

			// Check model
			assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
			assertEquals(3, model.count());
			assertEquals(CompoundLayout.of(Point.LAYOUT), model.layout());
			assertNotNull(model.vertices());
			assertEquals(false, model.isIndexed());
			assertEquals(Optional.empty(), model.index());

			// Check vertices
			final Bufferable vertices = model.vertices();
			assertNotNull(vertices);
			assertEquals(3 * Point.LAYOUT.length(), vertices.length());
		}

		@Test
		void indexed() {
			// Build indexed model
			final Model model = builder
					.layout(Point.LAYOUT)
					.add(vertex)
					.add(0)
					.add(0)
					.add(0)
					.build();

			// Check model
			assertEquals(true, model.isIndexed());
			assertEquals(3 * Short.BYTES, model.index().get().length());
		}

		@Test
		void restart() {
			//builder.restart().build();
			// TODO
		}
	}
}
