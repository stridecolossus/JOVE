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
		mesh = new BufferedMesh(Primitive.TRIANGLES, 3, new Layout(Point.LAYOUT), data, data);
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLES, mesh.primitive());
		assertEquals(3, mesh.count());
		assertEquals(new Layout(Point.LAYOUT), mesh.layout());
		assertEquals(true, mesh.isIndexed());
		assertEquals(data, mesh.vertices());
		assertEquals(Optional.of(data), mesh.index());
	}

	@Test
	void unindexed() {
		mesh = new BufferedMesh(Primitive.TRIANGLES, 3, new Layout(Point.LAYOUT), data, null);
		assertEquals(3, mesh.count());
		assertEquals(false, mesh.isIndexed());
		assertEquals(Optional.empty(), mesh.index());
	}
}
