package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.GridBuilder.HeightFunction;

public class GridBuilderTest {
	private GridBuilder builder;

	@BeforeEach
	void before() {
		builder = new GridBuilder();
	}

	@Test
	void literal() {
		final HeightFunction function = HeightFunction.literal(3);
		assertNotNull(function);
		assertEquals(3, function.height(1, 2));
	}

	@Test
	void build() {
		// Construct grid
		final Model model = builder
				.size(3)
				.width(4)
				.breadth(5)
				.build();

		// Check grid
		assertNotNull(model);
		assertEquals((2 * 2) * 4, model.count());
		assertEquals(Primitive.PATCH, model.primitive());
		assertEquals(List.of(Point.LAYOUT, Coordinate2D.LAYOUT), model.layout());
		assertEquals(true, model.isIndexed());

		// Check vertices
		assertEquals((3 * 3) * (3 + 2) * Float.BYTES, model.vertices().length());
		// TODO - check actual vertex data, but how?

		// Check index
		assertEquals(model.count() * Integer.BYTES, model.index().length());
	}
}
