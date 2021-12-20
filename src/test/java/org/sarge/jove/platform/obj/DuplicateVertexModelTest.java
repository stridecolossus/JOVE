package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Vertex;

class DuplicateVertexModelTest {
	private DuplicateVertexModel model;

	@BeforeEach
	void before() {
		model = new DuplicateVertexModel();
	}

	@Test
	void build() {
		// Create some vertices
		final Vertex vertex = new Vertex(Point.ORIGIN);
		final Vertex other = new Vertex(new Point(1, 2, 3));

		// Build an indexed model that re-uses some vertices
		model.add(vertex);
		model.add(other);
		model.add(vertex);

		// Verify the de-duplicated model
		assertEquals(true, model.isIndexed());
		assertEquals(3, model.count());
		assertArrayEquals(new Vertex[]{vertex, other}, model.vertices().toArray());
		assertArrayEquals(new int[]{0, 1, 0}, model.index().toArray());
	}
}
