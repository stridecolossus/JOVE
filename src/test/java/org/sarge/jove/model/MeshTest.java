package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;

class MeshTest {
	private Mesh mesh;
	private ByteSizedBufferable data;

	@BeforeEach
	void before() {
		data = mock(ByteSizedBufferable.class);
		mesh = new Mesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT, Normal.LAYOUT), () -> 3, data, null);
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE, mesh.primitive());
		assertEquals(new CompoundLayout(Point.LAYOUT, Normal.LAYOUT), mesh.layout());
		assertEquals(data, mesh.vertices());
	}

	@DisplayName("A mesh has a draw count")
	@Test
	void count() {
		assertEquals(3, mesh.count());
	}

	@DisplayName("The layout for a mesh must contain vertex positions")
	@Test
	void missingVertexPosition() {
		assertThrows(IllegalStateException.class, () -> new Mesh(Primitive.TRIANGLE, new CompoundLayout(), () -> 3, data, null));
	}

	@DisplayName("The draw count must match the primitive")
	@Test
	void invalidDrawCount() {
		assertThrows(IllegalStateException.class, () -> new Mesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT), () -> 2, data, null));
	}

	@DisplayName("A mesh containing vertex normals must be supported by the drawing primitive")
	@Test
	void invalidPrimitiveForNormals() {
		assertThrows(IllegalStateException.class, () -> new Mesh(Primitive.LINE, new CompoundLayout(Point.LAYOUT, Normal.LAYOUT), () -> 2, data, null));
	}

	@DisplayName("A default mesh does not have an index buffer")
	@Test
	void isIndexed() {
		assertEquals(false, mesh.isIndexed());
		assertEquals(Optional.empty(), mesh.index());
	}

	@DisplayName("An indexed mesh...")
	@Nested
	class IndexedMeshTests {
		@BeforeEach
		void before() {
			mesh = new Mesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT), () -> 3, data, data);
		}

		@DisplayName("has a draw count")
		@Test
		void count() {
			assertEquals(3, mesh.count());
		}

		@DisplayName("has an index buffer")
		@Test
		void isIndexed() {
			assertEquals(true, mesh.isIndexed());
			assertEquals(Optional.of(data), mesh.index());
		}
	}

	@Test
	void equals() {
		assertEquals(mesh, mesh);
		assertNotEquals(mesh, null);
	}
}
