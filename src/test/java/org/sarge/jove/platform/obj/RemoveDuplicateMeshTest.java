package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Vertex;

class RemoveDuplicateMeshTest {
	private RemoveDuplicateMesh mesh;

	@BeforeEach
	void before() {
		mesh = new RemoveDuplicateMesh(Point.LAYOUT);
	}

	@Test
	void build() {
		// Create some vertices
		final Vertex vertex = new Vertex(Point.ORIGIN);
		final Vertex other = new Vertex(new Point(1, 2, 3));

		// Build an indexed mesh that re-uses some vertices
		mesh.add(vertex);
		mesh.add(other);
		mesh.add(vertex);

		// Verify the de-duplicated model
		assertEquals(3, mesh.count());
		assertEquals(2 * 3 * 4, mesh.vertices().length());
		assertEquals(3 * 4, mesh.index().get().length());
	}
}
