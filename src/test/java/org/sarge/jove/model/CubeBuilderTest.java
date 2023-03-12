package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.CompoundLayout;
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
		final DefaultMesh mesh = builder.size(2).build();
		final int count = 6 * 2 * 3;
		final var layout = new CompoundLayout(Point.LAYOUT, Coordinate2D.LAYOUT);
		assertEquals(Primitive.TRIANGLE, mesh.primitive());
		assertEquals(count, mesh.count());
		assertEquals(layout, mesh.layout());
		assertEquals(false, mesh.isIndexed());
		assertEquals(count * layout.stride() , mesh.vertices().length());
	}
}
