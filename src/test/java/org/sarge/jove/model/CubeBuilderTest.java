package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;

public class CubeBuilderTest {
	private CubeBuilder builder;

	@BeforeEach
	void before() {
		builder = new CubeBuilder();
	}

	@Test
	void build() {
		final Model model = builder.size(2).build();
		final int count = 6 * 2 * 3;
		final var layout = new Layout(Point.LAYOUT, Coordinate2D.LAYOUT);
		assertEquals(Primitive.TRIANGLES, model.primitive());
		assertEquals(count, model.count());
		assertEquals(layout, model.layout());
		assertEquals(false, model.isIndexed());
		assertEquals(count * layout.stride() , model.vertices().length());
	}
}
