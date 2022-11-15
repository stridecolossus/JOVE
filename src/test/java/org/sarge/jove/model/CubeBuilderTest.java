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
		final DefaultMesh model = builder.size(2).build();
		final int count = 6 * 2 * 3;
		final var layout = new Layout(Point.LAYOUT, Coordinate2D.LAYOUT);
		assertEquals(Primitive.TRIANGLE, model.primitive());
		assertEquals(count, model.count());
		assertEquals(layout, model.layout());
		assertEquals(false, model.isIndexed());
		assertEquals(count * layout.stride() , model.buffer().vertices().length());
	}
}
