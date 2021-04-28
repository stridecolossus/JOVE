package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Component.Layout;
import org.sarge.jove.geometry.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Model.Header;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.obj.ObjectModel.VertexComponentList;

public class ObjectModelTest {
	private ObjectModel model;

	@BeforeEach
	void before() {
		model = new ObjectModel();
	}

	@Test
	void constructor() {
		assertEquals(true, model.isEmpty());
		assertNotNull(model.vertices());
		assertNotNull(model.normals());
		assertNotNull(model.coordinates());
		assertEquals(0, model.vertices().size());
		assertEquals(0, model.normals().size());
		assertEquals(0, model.coordinates().size());
	}

	@Test
	void vertices() {
		final var vertices = model.vertices();
		vertices.add(Point.ORIGIN);
		assertEquals(1, model.vertices().size());
		assertEquals(Point.ORIGIN, vertices.get(1));
		assertEquals(false, model.isEmpty());
	}

	@Test
	void normals() {
		final var normals = model.normals();
		normals.add(Vector.X_AXIS);
		assertEquals(1, normals.size());
		assertEquals(Vector.X_AXIS, normals.get(1));
	}

	@Test
	void coordinates() {
		final var coords = model.coordinates();
		coords.add(Coordinate2D.BOTTOM_LEFT);
		assertEquals(1, coords.size());
		assertEquals(Coordinate2D.BOTTOM_LEFT, coords.get(1));
	}

	@Nested
	class VertexTests {
		@BeforeEach
		void before() {
			model.vertices().add(Point.ORIGIN);
			model.normals().add(Vector.X_AXIS);
			model.coordinates().add(Coordinate2D.BOTTOM_LEFT);
		}

		@DisplayName("Add a vertex with all 3 components")
		@Test
		void face() {
			model.vertex(1, 1, 1);
		}

		@DisplayName("Add a vertex with just the position")
		@Test
		void faceVertexOnly() {
			model.vertex(1, null, null);
		}

		@DisplayName("Add a vertex with a normal")
		@Test
		void faceVertexNormal() {
			model.vertex(1, 1, null);
		}

		@DisplayName("Add a vertex with a texture coordinate")
		@Test
		void faceVertexTexture() {
			model.vertex(1, null, 1);
		}

		@Test
		void vertexInvalidIndex() {
			assertThrows(IndexOutOfBoundsException.class, () -> model.vertex(2, 1, 1));
		}
	}

	@Test
	void start() {
		model.vertices().add(Point.ORIGIN);
		model.start();
		assertEquals(true, model.isEmpty());
	}

	@Test
	void startEmptyModel() {
		model.start();
	}

	@Nested
	class ComponentListTests {
		private List<Object> list;
		private Object obj;

		@BeforeEach
		void before() {
			obj = new Object();
			list = new VertexComponentList<>();
		}

		@Test
		void constructor() {
			assertEquals(0, list.size());
		}

		@Test
		void get() {
			list.add(obj);
			assertEquals(obj, list.get(1));
		}

		@Test
		void getNegativeIndex() {
			list.add(obj);
			assertEquals(obj, list.get(-1));
		}

		@Test
		void getInvalidZeroIndex() {
			assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
		}

		@Test
		void getInvalidIndex() {
			assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
			assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
		}
	}

	@Nested
	class BuildTests {
		private void triangle() {
			model.vertices().add(Point.ORIGIN);
			model.normals().add(Vector.X_AXIS);
			model.coordinates().add(Coordinate2D.BOTTOM_LEFT);
			for(int n = 0; n < 3; ++n) {
				model.vertex(1, 1, 1);
			}
		}

		@Test
		void build() {
			// Build triangle model
			triangle();
			assertNotNull(model.build());
			assertEquals(1, model.build().count());

			// Check resultant model
			final Model result = model.build().iterator().next();
			assertNotNull(result);
			assertEquals(Optional.empty(), result.indexBuffer());

			// Check model header
			final Header header = result.header();
			assertNotNull(header);
			assertEquals(Primitive.TRIANGLES, header.primitive());
			assertEquals(3, header.count());
			assertEquals(true, header.clockwise());
			assertEquals(List.of(Layout.TUPLE, Layout.TUPLE, Coordinate2D.LAYOUT), header.layout());
		}

		@Test
		void buildMultiple() {
			// Add a group
			triangle();

			// Add another group
			model.start();
			triangle();

			// Construct and model per group
			assertEquals(2, model.build().count());
		}
	}
}
