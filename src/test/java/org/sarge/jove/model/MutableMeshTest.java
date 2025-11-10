package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.FloatBuffer;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.scene.volume.Bounds;

class MutableMeshTest {
	private MutableMesh mesh;

	@BeforeEach
	void before() {
		mesh = new MutableMesh(Primitive.TRIANGLE, List.of(Point.LAYOUT));
	}

	@Test
	void empty() {
		assertEquals(0, mesh.count());
		assertEquals(0, mesh.vertices().limit());
		assertEquals(Bounds.EMPTY, mesh.bounds());
	}

	@Test
	void add() {
		mesh.add(new Vertex(Point.ORIGIN));
		assertEquals(1, mesh.count());
		assertEquals(false, Primitive.TRIANGLE.isValidVertexCount(mesh.count()));
		assertEquals(3 * 4, mesh.vertices().limit());
	}

	@Test
	void face() {
		mesh.add(new Vertex(new Point(1, 2, 3)));
		mesh.add(new Vertex(new Point(4, 5, 6)));
		mesh.add(new Vertex(new Point(7, 8, 9)));
		assertEquals(3, mesh.count());
		assertEquals(true, Primitive.TRIANGLE.isValidVertexCount(mesh.count()));

		final FloatBuffer vertices = mesh.vertices().asFloatBuffer();
		assertEquals(9, vertices.limit());
		for(int n = 0; n < 9; ++n) {
			assertEquals(n + 1, vertices.get());
		}
	}

	@Test
	void index() {
		assertEquals(Optional.empty(), mesh.index());
	}

	@Test
	void bounds() {
		mesh.add(new Vertex(new Point(1, 0, 0)));
		mesh.add(new Vertex(new Point(0, 2, 0)));
		mesh.add(new Vertex(new Point(0, 0, 3)));
		assertEquals(new Bounds(Point.ORIGIN, new Point(1, 2, 3)), mesh.bounds());
	}
}
