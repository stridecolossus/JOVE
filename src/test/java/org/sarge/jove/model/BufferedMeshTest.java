package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Point;

class BufferedMeshTest {
	private BufferedMesh mesh;
	private ByteSizedBufferable data;

	@BeforeEach
	void before() {
		data = mock(ByteSizedBufferable.class);
		mesh = new BufferedMesh(Primitive.TRIANGLE, 3, new CompoundLayout(Point.LAYOUT), data, data);
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE, mesh.primitive());
		assertEquals(3, mesh.count());
		assertEquals(new CompoundLayout(Point.LAYOUT), mesh.layout());
		assertEquals(true, mesh.isIndexed());
		assertEquals(data, mesh.vertexBuffer());
		assertEquals(Optional.of(data), mesh.indexBuffer());
	}

	@Test
	void unindexed() {
		mesh = new BufferedMesh(Primitive.TRIANGLE, 3, new CompoundLayout(Point.LAYOUT), data, null);
		assertEquals(3, mesh.count());
		assertEquals(false, mesh.isIndexed());
		assertEquals(Optional.empty(), mesh.indexBuffer());
	}
}
