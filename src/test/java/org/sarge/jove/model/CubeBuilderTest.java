package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Coordinate.Coordinate2D;

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
		assertEquals(Primitive.TRIANGLES, model.header().primitive());
		assertEquals(count, model.header().count());
		assertEquals(layout, model.header().layout());
		assertEquals(false, model.header().isIndexed());
		assertEquals(count * layout.stride() , model.buffer().vertices().length());
	}
}
