package org.sarge.jove.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GridBuilderTest {
	private GridBuilder builder;

	@BeforeEach
	void before() {
		builder = new GridBuilder();
	}

	@Test
	void build() {
		/*
		final Model model = builder
				.size(2)
				.width(3)
				.breadth(4)
				.build(List.of(Point.LAYOUT));

		assertNotNull(model);
		assertEquals(new Header(List.of(Point.LAYOUT), Primitive.PATCH, 2 * 2), model.header());
		assertEquals(false, model.isIndexed());
		assertEquals(2 * 2 * Point.SIZE * Float.BYTES, model.vertices().length());
		*/
	}
}
