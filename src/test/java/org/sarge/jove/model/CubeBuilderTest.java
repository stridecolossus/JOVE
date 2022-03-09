package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
		final MutableModel cube = builder.size(2).build();
		final int count = 6 * 2 * 3;
		assertNotNull(cube);
		assertEquals(Primitive.TRIANGLES, cube.primitive());
		assertEquals(List.of(Point.LAYOUT, Coordinate2D.LAYOUT), cube.layout());
		assertEquals(count, cube.count());
		assertEquals(count, cube.vertices().count());
	}
}
