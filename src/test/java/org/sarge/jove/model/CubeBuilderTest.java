package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class CubeBuilderTest {
	private CubeBuilder builder;

	@BeforeEach
	void before() {
		builder = new CubeBuilder();
	}

	@Test
	void build() {
		// Build cube
		final Model cube = builder.size(2).build();

		// Check model
		assertNotNull(cube);
		assertEquals(false, cube.isIndexed());

		// Check header
		final int count = 6 * 2 * 3;
		assertNotNull(cube.header());
		assertEquals(List.of(Point.LAYOUT, Vector.NORMALS, Coordinate2D.LAYOUT, Colour.LAYOUT), cube.header().layout());
		assertEquals(Primitive.TRIANGLES, cube.header().primitive());
		assertEquals(count, cube.header().count());

		assertNotNull(cube.vertices());
		assertEquals(count * (3 + 3 + 2 + 4) * Float.BYTES, cube.vertices().length());
	}

	@Test
	void primitive() {
		assertThrows(UnsupportedOperationException.class, () -> builder.primitive(Primitive.LINES));
	}
}
