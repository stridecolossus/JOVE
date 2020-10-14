package org.sarge.jove.obj;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.ModelOLD;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.model.Vertex.MutableVertex;
import org.sarge.jove.obj.ObjectModel.Face;
import org.sarge.jove.obj.ObjectModel.Group;

public class ObjectModelTest {
	private ObjectModel model;

	@BeforeEach
	public void before() {
		model = new ObjectModel();
	}

	@Nested
	class GroupTests {
		private Group group;

		@BeforeEach
		public void before() {
			group = model.group();
		}

		@Test
		public void constructor() {
			assertNotNull(group);
			assertEquals(Optional.empty(), group.material());
		}

		@Test
		public void defaultGroup() {
			assertEquals("root", group.name());
			assertEquals(group, model.group());
			assertNotNull(model.groups());
			assertArrayEquals(new Group[]{group}, model.groups().toArray());
		}

		@Test
		public void newGroup() {
			// Add data to the default group
			group.vertex(Point.ORIGIN);

			// Add a new group
			final Group next = model.group("name");
			assertNotNull(next);
			assertEquals("name", next.name());
			assertEquals(next, model.group());

			// Check groups
			assertArrayEquals(new Group[]{group, next}, model.groups().toArray());
		}

		@Test
		public void newGroupReplacesDefaultGroup() {
			final Group next = model.group("name");
			assertArrayEquals(new Group[]{next}, model.groups().toArray());
		}

		@Test
		public void material() {
			final ObjectMaterial mat = mock(ObjectMaterial.class);
			group.material(mat);
			assertEquals(Optional.of(mat), group.material());
		}
	}

	@Nested
	class FaceTests {
		private Face face;

		@BeforeEach
		public void before() {
			face = new Face(1, 2, 3);
		}

		@Test
		public void constructor() {
			assertEquals(1, face.vertex());
			assertEquals(2, face.coords());
			assertEquals(3, face.normal());
		}

		@Test
		public void constructorVertexOnly() {
			face = new Face(1, 0, 0);
			assertEquals(1, face.vertex());
			assertEquals(0, face.coords());
			assertEquals(0, face.normal());
		}

		@Test
		public void equals() {
			assertTrue(face.equals(face));
			assertTrue(face.equals(new Face(1, 2, 3)));
			assertFalse(face.equals(null));
			assertFalse(face.equals(new Face(4, 5, 6)));
		}
	}

	@Nested
	class BuildTests {
		private Group group;

		@BeforeEach
		public void before() {
			group = model.group();
		}

		@Test
		public void vertexInvalidIndex() {
			final Face face = new Face(0, 1, 2);
			assertThrows(IllegalArgumentException.class, () -> group.face(face));
		}

		@Test
		public void build() {
			// Define a triangle
			final Point a = new Point(0, 1, 0);
			final Point b = new Point(0, 0, 0);
			final Point c = new Point(1, 0, 0);

			// Build OBJ model
			group.vertex(a);
			group.vertex(b);
			group.vertex(c);
			group.normal(Vector.Z_AXIS);
			group.face(new Face(1, 0, 1));
			group.face(new Face(2, 0, 1));
			group.face(new Face(3, 0, 1));

			// Create generic model
			final ModelOLD<MutableVertex> model = group.build();
			assertNotNull(model);
			assertEquals(Set.of(Vertex.Component.POSITION, Vertex.Component.NORMAL), model.components());
			assertEquals(false, model.isIndexed());

			// Check vertices
			final var vertices = model.vertices();
			checkVertex(vertices.get(0), a);
			checkVertex(vertices.get(1), b);
			checkVertex(vertices.get(2), c);
		}

		private void checkVertex(MutableVertex vertex, Point pos) {
			assertEquals(pos, vertex.position());
			assertEquals(Vector.Z_AXIS, vertex.normal());
			assertEquals(null, vertex.coordinates());
		}
	}
}
