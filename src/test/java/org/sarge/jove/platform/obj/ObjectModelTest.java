package org.sarge.jove.platform.obj;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;

public class ObjectModelTest {
	private ObjectModel model;

	@BeforeEach
	void before() {
		model = new ObjectModel();
	}

	@Test
	void builder() {
		assertNotNull(model.builder());
	}

	@Test
	void start() {
		model.position(Point.ORIGIN);
		model.start();
		assertEquals(2, model.build().count());
	}

	@Test
	void startEmpty() {
		model.start();
		assertEquals(1, model.build().count());
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
		@BeforeEach
		void before() {
			model.position(Point.ORIGIN);
			model.normal(Vector.X);
			model.coordinate(Coordinate2D.BOTTOM_LEFT);
		}

		@Test
		void build() {
			// Add a triangle
			for(int n = 0; n < 3; ++n) {
				model.vertex(1, 1, 1);
			}

			// Build models
			final List<Model> models = model.build().collect(toList());
			assertEquals(1, models.size());

			// Check model
			final Model result = models.get(0);
			assertNotNull(result);
			assertEquals(true, result.isIndexed());

			// Check model header
			final List<Layout> layout = List.of(Point.LAYOUT, Vector.LAYOUT, Coordinate2D.LAYOUT);
			assertEquals(new Model.Header(layout, Primitive.TRIANGLES, 3, true), result.header());
		}
	}
}
