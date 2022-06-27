package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Vertex;

class DuplicateModelTest {
	private DuplicateModel model;

	@BeforeEach
	void before() {
		model = new DuplicateModel();
	}

	@Test
	void build() {
		// Create some vertices
		final Vertex vertex = Vertex.of(Point.ORIGIN);
		final Vertex other = Vertex.of(new Point(1, 2, 3));

		// Build an indexed model that re-uses some vertices
		model.add(vertex);
		model.add(other);
		model.add(vertex);

		// Verify the de-duplicated model
		assertNotNull(model);
		assertEquals(3, model.count());
		assertArrayEquals(new Vertex[]{vertex, other}, model.vertices().toArray());
		assertArrayEquals(new int[]{0, 1, 0}, model.index().toArray());
	}
}