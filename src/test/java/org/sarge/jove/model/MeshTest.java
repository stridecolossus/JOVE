package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Normal;

class MeshTest {
	private static class MockAbstractModel extends Mesh {
		public MockAbstractModel(Primitive primitive) {
			super(primitive, new CompoundLayout(Normal.LAYOUT));
		}

		@Override
		public int count() {
			return 0;
		}

		@Override
		public boolean isIndexed() {
			return false;
		}

		@Override
		public ByteSizedBufferable vertexBuffer() {
			return null;
		}
	}

	private Mesh mesh;

	@BeforeEach
	void before() {
		mesh = new MockAbstractModel(Primitive.TRIANGLE);
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE, mesh.primitive());
		assertEquals(new CompoundLayout(Normal.LAYOUT), mesh.layout());
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new MockAbstractModel(Primitive.LINE));
	}

	@Test
	void equals() {
		assertEquals(mesh, mesh);
		assertEquals(mesh, new MockAbstractModel(Primitive.TRIANGLE));
		assertNotEquals(mesh, null);
		assertNotEquals(mesh, new MockAbstractModel(Primitive.TRIANGLE_STRIP));
	}
}
