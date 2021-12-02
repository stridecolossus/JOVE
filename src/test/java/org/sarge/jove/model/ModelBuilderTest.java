package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.Header;

class ModelBuilderTest {
	private ModelBuilder builder;
	private Vertex vertex;

	@BeforeEach
	void before() {
		builder = new ModelBuilder(List.of(Point.LAYOUT));
		vertex = Vertex.of(Point.ORIGIN);
	}

	@Test
	void build() {
		// Build model
		final Model model = builder
				.primitive(Primitive.LINES)
				.add(vertex)
				.add(vertex)
				.build();

		// Verify model
		assertNotNull(model);
		assertEquals(new Header(List.of(Point.LAYOUT), Primitive.LINES, 2), model.header());
		assertEquals(false, model.isIndexed());
		assertNotNull(model.vertices());
		assertEquals(Optional.empty(), model.index());
	}

	@Test
	void buildEmpty() {
		final Model model = builder.build();
		assertNotNull(model);
		assertEquals(new Header(List.of(Point.LAYOUT), Primitive.TRIANGLE_STRIP, 0), model.header());
		assertNotNull(model.vertices());
		assertEquals(Optional.empty(), model.index());
	}
}
