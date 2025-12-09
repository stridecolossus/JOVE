package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.*;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.scene.volume.Bounds;

class VertexMeshTest {
	private MutableMesh mesh;

	@BeforeEach
	void before() {
		mesh = new MutableMesh(Primitive.TRIANGLE, List.of(Point.LAYOUT));
	}

	@Test
	void empty() {
		assertEquals(0, mesh.count());
		assertEquals(0, mesh.vertices().length());
		assertEquals(Bounds.EMPTY, mesh.bounds());
	}

	@Test
	void add() {
		mesh.add(new Vertex(Point.ORIGIN));
		assertEquals(1, mesh.count());
	}

	@Test
	void buffer() {
		// Build mesh
		mesh.add(new Vertex(new Point(1, 2, 3)));
		mesh.add(new Vertex(new Point(4, 5, 6)));
		mesh.add(new Vertex(new Point(7, 8, 9)));

		// Write to buffer
		final var vertices = mesh.vertices();
		final ByteBuffer buffer = ByteBuffer.allocate(3 * 3 * Float.BYTES);
		assertEquals(vertices.length(), buffer.limit());
		vertices.buffer(buffer);
		assertEquals(0, buffer.remaining());

		// Check buffer
		final FloatBuffer fb = buffer.rewind().asFloatBuffer();
		buffer.rewind();
		for(int n = 0; n < 9; ++n) {
			assertEquals(n + 1, fb.get());
		}
	}

	@Test
	void bounds() {
		mesh.add(new Vertex(new Point(1, 0, 0)));
		mesh.add(new Vertex(new Point(0, 2, 0)));
		mesh.add(new Vertex(new Point(0, 0, 3)));
		assertEquals(new Bounds(Point.ORIGIN, new Point(1, 2, 3)), mesh.bounds());
	}
}
