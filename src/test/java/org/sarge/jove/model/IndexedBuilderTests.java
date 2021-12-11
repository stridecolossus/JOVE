package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;

public class IndexedBuilderTests {
	private IndexedBuilder model;

	@BeforeEach
	void before() {
		model = new IndexedBuilder(Primitive.TRIANGLES);
	}

	@Test
	void build() {
		// Build an indexed model that re-uses some vertices
		final Vertex vertex = new Vertex(Point.ORIGIN);
		final Vertex other = new Vertex(new Point(1, 2, 3));
		model.layout(Point.LAYOUT);
		model.add(vertex);
		model.add(other);
		model.add(vertex);

		// Verify the de-duplicated model
		assertEquals(true, model.isIndexed());
		assertEquals(3, model.count());
		assertEquals(List.of(vertex, other), model.vertices);
		assertEquals(List.of(0, 1, 0), model.index);
	}
}
