package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

class CubeTest {
	private Cube cube;

	@BeforeEach
	void before() {
		cube = new Cube();
	}

	@Test
	void build() {
		final Mesh mesh = cube.build(2);
		final int count = 6 * 2 * 3;
		final var layout = List.of(Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT);
		assertEquals(Primitive.TRIANGLE, mesh.primitive());
		assertEquals(count, mesh.count());
		assertEquals(layout, mesh.layout());
		assertEquals(count * (3 + 3 + 2) * 4, mesh.vertices().length());
	}
}
