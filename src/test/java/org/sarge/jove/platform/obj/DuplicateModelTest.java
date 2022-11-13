package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Vertex;

class DuplicateModelTest {
	private DuplicateModel model;

	@BeforeEach
	void before() {
		model = new DuplicateModel(new Layout(Point.LAYOUT));
	}

	@Test
	void build() {
		// Create some vertices
		final Vertex vertex = Point.ORIGIN;
		final Vertex other = new Point(1, 2, 3);

		// Build an indexed model that re-uses some vertices
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
