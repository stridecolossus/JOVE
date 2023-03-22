package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

public class ObjectModelTest {
	private ObjectModel model;

	@BeforeEach
	void before() {
		model = new ObjectModel();
		model.positions().add(Point.ORIGIN);
	}

	@Nested
	class VertexTests {
		@DisplayName("Add a vertex with all 3 components")
		@Test
		void vertexAll() {
			model.normals().add(Axis.X);
			model.coordinates().add(Coordinate2D.BOTTOM_LEFT);
			model.start();
			model.vertex(new int[]{1, 1, 1});
		}

		@DisplayName("Add a vertex with just the position")
		@Test
		void vertexOnly() {
			model.start();
			model.vertex(new int[]{1, 0, 0});
		}

		@DisplayName("Add a vertex with a normal")
		@Test
		void vertexNormal() {
			model.normals().add(Axis.X);
			model.start();
			model.vertex(new int[]{1, 0, 1});
		}

		@DisplayName("Add a vertex with a texture coordinate")
		@Test
		void vertexTexture() {
			model.coordinates().add(Coordinate2D.BOTTOM_LEFT);
			model.start();
			model.vertex(new int[]{1, 1, 0});
		}

		@Test
		void vertexNegativeIndex() {
			model.start();
			model.vertex(new int[]{-1, 0, 0});
		}

		@Test
		void vertexInvalidIndex() {
			model.start();
			assertThrows(IndexOutOfBoundsException.class, () -> model.vertex(new int[]{0, 0, 0}));
			assertThrows(IndexOutOfBoundsException.class, () -> model.vertex(new int[]{2, 0, 0}));
		}
	}

	@Nested
	class BuilderTests {
		private void triangle() {
			model.normals().add(Axis.X);
			model.coordinates().add(Coordinate2D.BOTTOM_LEFT);
			model.start();
			for(int n = 0; n < 3; ++n) {
				model.vertex(new int[]{1, 1, 1});
			}
		}

		@Test
		void build() {
			// Add a triangle
			triangle();

			// Build models
			final List<Mesh> models = model.models();
			assertEquals(1, models.size());

			// Check model
			final Mesh mesh = models.get(0);
			final var layout = new CompoundLayout(Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT);
			assertEquals(Primitive.TRIANGLE, mesh.primitive());
			assertEquals(3, mesh.count());
			assertEquals(layout, mesh.layout());

			// Check model data
			assertEquals((3 + 3 + 2) * Float.BYTES, mesh.vertices().length());
			assertEquals(3 * Short.BYTES, mesh.index().get().length());
		}

		// TODO
		@Disabled
		@Test
		void start() {
			model.start();
			triangle();
			model.start();
			assertEquals(true, model.positions().isEmpty());
			assertEquals(true, model.normals().isEmpty());
			assertEquals(true, model.coordinates().isEmpty());
			assertEquals(2, model.models().size());
		}
	}
}
