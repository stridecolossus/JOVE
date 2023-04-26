package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

public class CubeBuilderTest {
	private CubeBuilder builder;

	@BeforeEach
	void before() {
		builder = new CubeBuilder();
	}

	@Test
	void build() {
		final Mesh mesh = builder.size(2).build();
		final int count = 6 * 2 * 3;
		final var layout = new CompoundLayout(Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT);
		assertEquals(Primitive.TRIANGLE, mesh.primitive());
		assertEquals(count, mesh.count());
		assertEquals(layout, mesh.layout());
		assertEquals(count * layout.stride() , mesh.vertices().length());
	}

	@Test
	void test() {
		for(int n = 0; n < 8; ++n) {
			float x = (n & 2) != 0 ? +1 : -1;
			float y = (n & 1) != 0 ? +1 : -1;
			float z = (n & 4) != 0 ? -1 : +1;
			System.out.println(n+" "+x+","+y+","+z);
		}
	}
}
