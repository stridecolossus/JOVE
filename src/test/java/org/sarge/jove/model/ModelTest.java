package org.sarge.jove.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model.Builder;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.model.Vertex.MutableVertex;

public class ModelTest {
	private MutableVertex a, b, c;

	@BeforeEach
	public void before() {
		a = new MutableVertex(new Point(0, 1, 0));
		b = new MutableVertex(new Point(0, 0, 0));
		c = new MutableVertex(new Point(1, 1, 0));
	}

	@Nested
	class DefaultModelTests {
		private Model<MutableVertex> model;
		private MutableVertex d;

		@BeforeEach
		public void before() {
			d = new MutableVertex(new Point(1, 0, 0));
			model = new Model.Builder<>()
				.primitive(Primitive.TRIANGLE_STRIP)
				.add(a)
				.add(b)
				.add(c)
				.add(d)
				.build();
		}

		@Test
		public void constructor() {
			assertNotNull(model);
			assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
			assertEquals(false, model.isIndexed());
			assertEquals(List.of(Component.POSITION), model.components());
			assertEquals(4, model.length());
		}

		@Test
		public void vertices() {
			assertNotNull(model.vertices());
			assertArrayEquals(new Vertex[]{a, b, c, d}, model.vertices().toArray());
		}

		@Test
		public void extents() {
			assertEquals(new Extents(Point.ORIGIN, c.position()), model.extents());
		}

		@Test
		public void faces() {
			final var faces = model.faces();
			assertNotNull(faces);
			assertEquals(List.of(a, b, c), faces.next());
			assertEquals(List.of(b, c, d), faces.next());
			assertEquals(false, faces.hasNext());
		}

		@Test
		public void computeNormals() {
			model.computeNormals();
			assertEquals(true, model.components().contains(Vertex.Component.NORMAL));
			assertEquals(true, model.vertices().stream().map(MutableVertex::normal).allMatch(Vector.Z_AXIS::equals));
		}

		@Test
		public void computeNormalsInvalidPrimitive() {
			model = new Model.Builder<>().primitive(Primitive.LINE_LIST).add(a).add(b).build();
			assertThrows(IllegalStateException.class, () -> model.computeNormals());
		}

		@Test
		public void computeNormalsAlreadyPresent() {
			model.computeNormals();
			assertThrows(IllegalStateException.class, () -> model.computeNormals());
		}
	}

	@Nested
	class BuilderTests {
		private Builder<MutableVertex> builder;

		@BeforeEach
		public void before() {
			builder = new Builder<>();
		}

		/**
		 * Adds a triangle.
		 */
		private void triangle() {
			builder.add(a);
			builder.add(b);
			builder.add(c);
		}

		@Test
		public void invalidModelNotEmpty() {
			builder.add(a);
			assertThrows(IllegalStateException.class, () -> builder.primitive(Primitive.TRIANGLE_LIST));
			assertThrows(IllegalStateException.class, () -> builder.component(Vertex.Component.COLOUR));
		}

		@Test
		public void invalidNormalsForPrimitive() {
			builder.primitive(Primitive.LINE_LIST);
			assertThrows(IllegalStateException.class, () -> builder.component(Vertex.Component.NORMAL));
		}

		@Test
		public void addVertex() {
			// Add a triangle
			triangle();

			// Build model
			final Model<?> model = builder.build();
			assertNotNull(model);
			assertEquals(Primitive.TRIANGLE_LIST, model.primitive());

			// Check vertices
			assertNotNull(model.vertices());
			assertArrayEquals(new Vertex[]{a, b, c}, model.vertices().toArray());
		}

		@Test
		public void buildEmptyModel() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		public void buildIncompleteVertices() {
			builder.add(a);
			builder.add(b);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
