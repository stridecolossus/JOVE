package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;

class RemoveDuplicateMeshTest {
	private RemoveDuplicateMesh model;

	@BeforeEach
	void before() {
		model = new RemoveDuplicateMesh(new CompoundLayout(Point.LAYOUT));
	}

	@Test
	void build() {
		// Create some vertices
		final Vertex vertex = new SimpleVertex(Point.ORIGIN);
		final Vertex other = new SimpleVertex(new Point(1, 2, 3));

		// Build an indexed mesh that re-uses some vertices
		model
				.add(vertex)
				.add(other)
				.add(vertex);

		// Verify the de-duplicated model
		assertEquals(3, model.count());
		assertEquals(2, model.vertices().count());
		assertEquals(3, model.index().count());
	}
}
