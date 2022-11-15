package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Normal;

public class AbstractMeshTest {
	private static class MockAbstractModel extends AbstractMesh {
		public MockAbstractModel(Primitive primitive) {
			super(primitive, new Layout(Normal.LAYOUT));
		}

		@Override
		public int count() {
			return 0;
		}

		@Override
		public boolean isIndexed() {
			return false;
		}
	}

	private AbstractMesh mesh;

	@BeforeEach
	void before() {
		mesh = new MockAbstractModel(Primitive.TRIANGLES);
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLES, mesh.primitive());
		assertEquals(new Layout(Normal.LAYOUT), mesh.layout());
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new MockAbstractModel(Primitive.LINES));
	}

	@Test
	void equals() {
		assertEquals(mesh, mesh);
		assertEquals(mesh, new MockAbstractModel(Primitive.TRIANGLES));
		assertNotEquals(mesh, null);
		assertNotEquals(mesh, new MockAbstractModel(Primitive.TRIANGLE_STRIP));
	}
}
