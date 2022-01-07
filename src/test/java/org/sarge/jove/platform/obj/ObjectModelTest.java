package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;

public class ObjectModelTest {
	private ObjectModel model;

	@BeforeEach
	void before() {
		model = new ObjectModel();
	}

	@Nested
	class VertexTests {
		@BeforeEach
		void before() {
			model.position(Point.ORIGIN);
			model.normal(Vector.X);
			model.coordinate(Coordinate2D.BOTTOM_LEFT);
		}

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
			// Add vertex data
			model.position(Point.ORIGIN);
			model.normal(Vector.X);
			model.coordinate(Coordinate2D.BOTTOM_LEFT);

			// Add face
			for(int n = 0; n < 3; ++n) {
				model.vertex(1, 1, 1);
			}
		}

		@Test
		void build() {
			// Add a triangle
			triangle();

			// Build models
			final List<Model> models = model.build();
			assertEquals(1, models.size());

			// Check model
			final Model result = models.get(0);
			assertNotNull(result);

			// Check model header
			assertEquals(Primitive.TRIANGLES, result.primitive());
			assertEquals(List.of(Point.LAYOUT, Vertex.NORMALS, Coordinate2D.LAYOUT), result.layout());
			assertEquals(3, result.count());
			assertEquals(true, result.isIndexed());

			// Check model data
			assertEquals((3 + 3 + 2) * Float.BYTES, result.vertexBuffer().length());
			assertEquals(3 * Integer.BYTES, result.indexBuffer().length());
		}

		@Test
		void start() {
			triangle();
			model.start();
			triangle();
			assertEquals(2, model.build().size());
		}
	}
}
