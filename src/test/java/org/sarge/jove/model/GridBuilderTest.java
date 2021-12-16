package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.GridBuilder.HeightFunction;

class GridBuilderTest {
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

	@DisplayName("Create an indexed grid comprising two triangles per quad")
	@Test
	void buildTriangles() {
		// Construct grid
		final Model model = builder
				.size(new Dimensions(2, 3))
				.tile(4)
				.build();

		// Check grid
		assertNotNull(model);
		assertEquals(2 * (2 * 3), model.count());
		assertEquals(true, model.isIndexed());
		assertEquals(Primitive.TRIANGLES, model.primitive());
		assertEquals(List.of(Point.LAYOUT, Coordinate2D.LAYOUT), model.layout());
	}

	@DisplayName("Create an unindexed grid comprising two triangles per quad")
	@Test
	void buildEmptyIndex() {
		// Construct grid
		final Model model = builder
				.size(new Dimensions(2, 3))
				.index(null)
				.build();

		// Check grid
		assertNotNull(model);
		assertEquals(2 * (2 * 3), model.count());
		assertEquals(false, model.isIndexed());
	}

	// TODO - triangle strip + degenerates

	@DisplayName("Create an indexed grid of patch control points comprised of quads")
	@Test
	void buildQuadStrip() {
		// Construct grid
		final Model model = builder
				.size(new Dimensions(2, 3))
				.primitive(Primitive.PATCH)
				.index(Quad.STRIP)
				.build();

		// Check grid
		assertNotNull(model);
		assertEquals(2 * 4, model.count());
		assertEquals(Primitive.PATCH, model.primitive());
		assertEquals(List.of(Point.LAYOUT, Coordinate2D.LAYOUT), model.layout());
		assertEquals(true, model.isIndexed());

		// Check vertices
		assertEquals((2 * 3) * (3 + 2) * Float.BYTES, model.vertices().length());
		// TODO - check actual vertex data, but how?

		// Check index
		assertEquals((2 * 4) * Integer.BYTES, model.index().length());
		// TODO - 0341 1452 3674 4785
	}
}
