package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.GridBuilder.HeightFunction;

class GridBuilderTest {
	private GridBuilder builder;

	@BeforeEach
	void before() {
		builder = new GridBuilder();
	}

	@DisplayName("Create a grid with an overridden index factory (patch control points comprising quads)")
	@Test
	void buildQuadStrip() {
		final DefaultMesh model = builder.primitive(Primitive.PATCH).index(IndexFactory.QUADS).build();
		assertEquals(Primitive.PATCH, model.primitive());
		assertEquals((3 * 3) * 4, model.count());
		assertEquals(new Layout(Point.LAYOUT, Coordinate2D.LAYOUT), model.layout());
		assertEquals(true, model.isIndexed());
	}

	@DisplayName("Create a grid comprising a triangle strip with degenerate triangles")
	@Test
	void buildTriangleStrip() {
		// TODO
	}

	@Nested
	class HeightFunctionTest {
		private ImageData image;

		@BeforeEach
		void before() {
			final Component layout = new Component(4, Component.Type.INTEGER, false, 2);
			image = new ImageData(new Dimensions(8, 8), "RGBA", layout, new byte[8 * 8 * (4 * 2)]) {
				@Override
				protected int pixel(int index) {
					return 65535;
				}
			};
		}

		@Test
		void literal() {
			final HeightFunction function = HeightFunction.literal(3);
			assertEquals(3, function.height(1, 2));
		}

		@Test
		void image() {
			final HeightFunction function = HeightFunction.heightmap(new Dimensions(4, 4), image, 1, 2);
			assertEquals(2, function.height(1, 1));
		}

		@Test
		void imageInvalidComponentIndex() {
			assertThrows(IllegalArgumentException.class, () -> HeightFunction.heightmap(new Dimensions(4, 4), image, 999, 1));
		}
	}
}
