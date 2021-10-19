package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Vertex;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.Header;

public class IndexedBuilderTests {
	private IndexedBuilder builder;

	@BeforeEach
	void before() {
		builder = new IndexedBuilder();
	}

	@Test
	void build() {
		// Build an indexed model that re-uses some vertices
		final Vertex vertex = Vertex.of(Point.ORIGIN);
		final Vertex other = Vertex.of(new Point(1, 2, 3));
		final DefaultModel model = builder
				.add(vertex)
				.add(other)
				.add(vertex)
				.build();

		// Verify the indexed model
		assertNotNull(model);
		assertEquals(true, model.isIndexed());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
		assertEquals(true, model.index().isPresent());

		// Check model header
		final Header header = model.header();
		assertNotNull(header);
		assertEquals(3, header.count());

		// Check index
		final Bufferable index = model.index().get();
		assertNotNull(index);
		assertEquals(3 * Integer.BYTES, index.length());
	}

	@Test
	void buildEmpty() {
		final DefaultModel model = builder.build();
		assertNotNull(model);
		assertEquals(0, model.header().count());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
	}
}
