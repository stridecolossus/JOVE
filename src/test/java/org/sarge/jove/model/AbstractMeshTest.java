package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;

class AbstractMeshTest {
	private static class MockAbstractMesh extends AbstractMesh {
		private final int count;

		public MockAbstractMesh(int count, Primitive primitive, Layout... layout) {
			super(primitive, List.of(layout));
			this.count = count;
		}

		@Override
		public ByteBuffer vertices() {
			return null;
		}

		@Override
		public int count() {
			return count;
		}
	}

	@Test
	void position() {
		assertThrows(IllegalArgumentException.class, () -> new MockAbstractMesh(3, Primitive.TRIANGLE, Normal.LAYOUT));
	}

	@Test
	void normals() {
		assertThrows(IllegalArgumentException.class, () -> new MockAbstractMesh(1, Primitive.POINT, Point.LAYOUT, Normal.LAYOUT));
		assertThrows(IllegalArgumentException.class, () -> new MockAbstractMesh(2, Primitive.LINE, Point.LAYOUT, Normal.LAYOUT));
		assertThrows(IllegalArgumentException.class, () -> new MockAbstractMesh(2, Primitive.LINE_STRIP, Point.LAYOUT, Normal.LAYOUT));
		assertThrows(IllegalArgumentException.class, () -> new MockAbstractMesh(1, Primitive.PATCH, Point.LAYOUT, Normal.LAYOUT));
	}
}
