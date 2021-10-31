package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class CubeBuilderTest {
	private CubeBuilder builder;

	@BeforeEach
	void before() {
		builder = new CubeBuilder();
	}

	@Test
	void invalidVertexComponent() {
		assertThrows(IllegalArgumentException.class, () -> builder.layout(Layout.of(3)));
	}

	@Test
	void duplicatePositionComponent() {
		assertThrows(IllegalArgumentException.class, () -> builder.layout(Point.LAYOUT));
	}

	@Test
	void build() {
		// Build cube
		final Model cube = builder
				.size(2)
				.layout(Vector.NORMALS)
				.layout(Coordinate2D.LAYOUT)
				.validate(true)
				.build();

		// Check model
		assertNotNull(cube);
		assertEquals(false, cube.isIndexed());

		// Check header
		final int count = 6 * 2 * 3;
		assertNotNull(cube.header());
		assertEquals(CompoundLayout.of(Point.LAYOUT, Vector.NORMALS, Coordinate2D.LAYOUT), cube.header().layout());
		assertEquals(Primitive.TRIANGLES, cube.header().primitive());
		assertEquals(count, cube.header().count());

		assertNotNull(cube.vertices());
		assertEquals(count * (3 + 3 + 2) * Float.BYTES, cube.vertices().length());
	}
}
