package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout;
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
		final Model cube = builder.size(2).build();
		final int count = 6 * 2 * 3;
		final var layout = List.of(Point.LAYOUT, Coordinate2D.LAYOUT);
		assertNotNull(cube);
		assertEquals(new Header(Primitive.TRIANGLES, count, layout), cube.header());
		assertEquals(count * Layout.stride(layout) , cube.vertices().length());
	}
}
