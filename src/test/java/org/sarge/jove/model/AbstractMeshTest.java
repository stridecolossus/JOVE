package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;

class AbstractMeshTest {
	private static class MockAbstractMesh extends AbstractMesh {
		public MockAbstractMesh(Primitive primitive, Layout... layout) {
			super(primitive, List.of(layout));
		}

		@Override
		public int count() {
			return 0;
		}

		@Override
		public DataBuffer vertices() {
			return null;
		}
	}

	@Test
	void position() {
		assertThrows(IllegalArgumentException.class, () -> new MockAbstractMesh(Primitive.TRIANGLE, Normal.LAYOUT));
	}

	@ParameterizedTest
	@EnumSource(names={"POINT", "LINE", "LINE_STRIP", "PATCH"})
	void normals(Primitive primitive) {
		assertThrows(IllegalArgumentException.class, () -> new MockAbstractMesh(primitive, Point.LAYOUT, Normal.LAYOUT));
	}
}
