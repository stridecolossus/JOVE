package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;

class RemoveDuplicateMeshTest {
	private RemoveDuplicateMeshBuilder builder;

	@BeforeEach
	void before() {
		builder = new RemoveDuplicateMeshBuilder(new CompoundLayout(Point.LAYOUT));
	}

	@Test
	void build() {
		// Create some vertices
		final Vertex vertex = new Vertex(Point.ORIGIN);
		final Vertex other = new Vertex(new Point(1, 2, 3));

		// Build an indexed mesh that re-uses some vertices
		builder
				.add(vertex)
				.add(other)
				.add(vertex);

		// Verify the de-duplicated model
		final Mesh mesh = builder.mesh();
		assertEquals(3, mesh.count());
		assertEquals(2 * 3 * Float.BYTES, mesh.vertices().length());
		assertEquals(3 * Short.BYTES, mesh.index().get().length());
	}
}
