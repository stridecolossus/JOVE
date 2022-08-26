package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.*;

class ModelTest {
	private Header header;
	private Bufferable data;
	private Vertex vertex;

	@BeforeEach
	void before() {
		header = new Header(Primitive.TRIANGLE_STRIP, 3, CompoundLayout.of(Point.LAYOUT));
		data = mock(Bufferable.class);
		vertex = Vertex.of(Point.ORIGIN);
	}

	@Nested
	class HeaderTests {
		@Test
		void constructor() {
			assertEquals(Primitive.TRIANGLE_STRIP, header.primitive());
			assertEquals(3, header.count());
			assertEquals(CompoundLayout.of(Point.LAYOUT), header.layout());
		}

		@DisplayName("The draw count must logically match the drawing primitive")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> new Header(Primitive.TRIANGLES, 2, CompoundLayout.of(Point.LAYOUT)));
		}

		@DisplayName("A model cannot contain normals if unsupported by the drawing primitive")
		@Test
		void normals() {
			assertThrows(IllegalArgumentException.class, () -> new Header(Primitive.LINES, 2, CompoundLayout.of(Point.LAYOUT, Model.NORMALS)));
		}

		@DisplayName("The index buffer for a model can be stored as integer or short values")
		@Test
		void isIntegerIndex() {
			final int max = 65535;
			assertEquals(false, Header.isIntegerIndex(0));
			assertEquals(false, Header.isIntegerIndex(max - 1));
			assertEquals(true, Header.isIntegerIndex(max));
		}
	}

	@DisplayName("An unindexed model does not have an index buffer")
	@Test
	void unindexed() {
		final Model model = new DefaultModel(header, data, null);
		assertEquals(header, model.header());
		assertEquals(data, model.vertices());
		assertEquals(Optional.empty(), model.index());
	}

	@DisplayName("An indexed model also contains an index buffer")
	@Test
	void indexed() {
		final Model model = new DefaultModel(header, data, data);
		assertEquals(header, model.header());
		assertEquals(data, model.vertices());
		assertEquals(Optional.of(data), model.index());
	}

	@Nested
	class BuilderTests {
		private Model.Builder builder;

		@BeforeEach
		void before() {
			builder = new Model.Builder();
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
			assertNotNull(model);
			assertEquals(header, model.header());
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
			assertNotNull(model);
			assertEquals(header, model.header());

			// Check vertices
			final Bufferable vertices = model.vertices();
			assertNotNull(vertices);
			assertEquals(Point.LAYOUT.length(), vertices.length());

			// Check index
			assertEquals(3 * Short.BYTES, model.index().get().length());
		}

		@Test
		void restart() {
			//builder.restart().build();
			// TODO
		}
	}
}
