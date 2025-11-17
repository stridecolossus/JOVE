package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.Vertex;

class RemoveDuplicateMeshTest {
	private RemoveDuplicateMesh mesh;

	@BeforeEach
	void before() {
		mesh = new RemoveDuplicateMesh();
	}

	@Test
	void build() {
		// Create some vertices
		final Vertex vertex = new Vertex(Point.ORIGIN, Axis.X, Coordinate2D.BOTTOM_LEFT);
		final Vertex other = new Vertex(new Point(1, 2, 3), Axis.Y, Coordinate2D.TOP_RIGHT);

		// Build an indexed mesh that re-uses some vertices
		mesh
				.add(vertex)
				.add(other)
				.add(vertex);

		// Verify the de-duplicated model
		assertEquals(3, mesh.count());
		assertEquals(2 * (3 + 3 + 2), mesh.vertices().length());
		assertEquals(3, mesh.index().length());
	}
}
