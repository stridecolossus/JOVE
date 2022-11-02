package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

public class ObjectModelTest {
	private ObjectModel model;

	@BeforeEach
	void before() {
		model = new ObjectModel();
		model.positions().add(Point.ORIGIN);
		model.normals().add(Axis.X);
		model.coordinates().add(Coordinate2D.BOTTOM_LEFT);
	}

	@Nested
	class VertexTests {
		@DisplayName("Add a vertex with all 3 components")
		@Test
		void vertexAll() {
			model.vertex(1, 1, 1);
		}

		@DisplayName("Add a vertex with just the position")
		@Test
		void vertexOnly() {
			model.vertex(1, null, null);
		}

		@DisplayName("Add a vertex with a normal")
		@Test
		void vertexNormal() {
			model.vertex(1, null, 1);
		}

		@DisplayName("Add a vertex with a texture coordinate")
		@Test
		void vertexTexture() {
			model.vertex(1, 1, null);
		}

		@Test
		void vertexNegativeIndex() {
			model.vertex(-1, null, null);
		}

		@Test
		void vertexInvalidIndex() {
			assertThrows(IndexOutOfBoundsException.class, () -> model.vertex(0, null, null));
			assertThrows(IndexOutOfBoundsException.class, () -> model.vertex(2, null, null));
		}
	}

	@Nested
	class BuilderTests {
		private void triangle() {
			for(int n = 0; n < 3; ++n) {
				model.vertex(1, 1, 1);
			}
		}

		@Test
		void build() {
			// Add a triangle
			triangle();

			// Build models
			final List<Model> models = model.models();
			assertEquals(1, models.size());

			// Check model
			final Model result = models.get(0);
			assertTrue(result.isIndexed());

			// Check model header
			final var layout = new Layout(Point.LAYOUT, Model.NORMALS, Coordinate2D.LAYOUT);
			assertEquals(Primitive.TRIANGLES, result.primitive());
			assertEquals(3, result.count());
			assertEquals(layout, result.layout());
			assertEquals(true, result.isIndexed());

			// Check model data
			final BufferedModel buffer = result.buffer();
			assertEquals((3 + 3 + 2) * Float.BYTES, buffer.vertices().length());
			assertEquals(3 * Short.BYTES, buffer.index().get().length());
		}

		@Test
		void start() {
			triangle();
			model.start();
			assertEquals(true, model.positions().isEmpty());
			assertEquals(true, model.normals().isEmpty());
			assertEquals(true, model.coordinates().isEmpty());
			assertEquals(2, model.models().size());
		}
	}
}
