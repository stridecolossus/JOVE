package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.Header;

public class CubeBuilderTest {
	private CubeBuilder builder;

	@BeforeEach
	void before() {
		builder = new CubeBuilder();
	}

	@Test
	void build() {
		final var layout = List.of(Point.LAYOUT, Coordinate2D.LAYOUT);
		final Model cube = builder.build();
		assertNotNull(cube);
		assertEquals(new Header(layout, Primitive.TRIANGLES, (2 * 3) * 6, false), cube.header());
		assertEquals(false, cube.isIndexed());
	}
}
