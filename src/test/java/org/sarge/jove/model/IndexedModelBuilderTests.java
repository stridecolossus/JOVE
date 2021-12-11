package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;

public class IndexedModelBuilderTests {
	private IndexedModelBuilder builder;

	@BeforeEach
	void before() {
		builder = new IndexedModelBuilder();
	}

	@Test
	void build() {
		// Create some vertices
		final Vertex vertex = new Vertex(Point.ORIGIN);
		final Vertex other = new Vertex(new Point(1, 2, 3));

		// Build an indexed model that re-uses some vertices
		final Model model = builder
				.layout(Point.LAYOUT)
				.add(vertex)
				.add(other)
				.add(vertex)
				.build();

		// Verify the de-duplicated model
		assertEquals(true, model.isIndexed());
		assertEquals(3, model.count());
		assertEquals(2 * 3 * Float.BYTES, model.vertices().length());
		assertEquals(3 * Integer.BYTES, model.index().length());
	}
}
