package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;

class MeshTest {
	private DefaultMesh mesh;
	private ByteSizedBufferable vertices;

	@BeforeEach
	void before() {
		vertices = mock(ByteSizedBufferable.class);
		mesh = new DefaultMesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT, Normal.LAYOUT), 3, vertices, null);
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE, mesh.primitive());
		assertEquals(new CompoundLayout(Point.LAYOUT, Normal.LAYOUT), mesh.layout());
		assertEquals(vertices, mesh.vertices());
	}

	@DisplayName("A mesh has a draw count")
	@Test
	void count() {
		assertEquals(3, mesh.count());
	}

	@DisplayName("The layout for a mesh must contain vertex positions")
	@Test
	void missingVertexPosition() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultMesh(Primitive.TRIANGLE, new CompoundLayout(), 3, vertices, null));
	}

	@DisplayName("The draw count must match the primitive")
	@Test
	void invalidDrawCount() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultMesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT), 2, vertices, null));
	}

	@DisplayName("A mesh containing vertex normals must be supported by the drawing primitive")
	@Test
	void invalidPrimitiveForNormals() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultMesh(Primitive.LINE, new CompoundLayout(Point.LAYOUT, Normal.LAYOUT), 2, vertices, null));
	}

	@DisplayName("A default mesh does not have an index buffer")
	@Test
	void isIndexed() {
		assertEquals(Optional.empty(), mesh.index());
	}

	@DisplayName("An indexed mesh...")
	@Nested
	class IndexedMeshTests {
		private ByteSizedBufferable index;

		@BeforeEach
		void before() {
			index = mock(ByteSizedBufferable.class);
			mesh = new DefaultMesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT), 3, vertices, index);
		}

		@DisplayName("has a draw count")
		@Test
		void count() {
			assertEquals(3, mesh.count());
		}

		@DisplayName("has an index buffer")
		@Test
		void isIndexed() {
			assertEquals(Optional.of(index), mesh.index());
		}
	}
}
