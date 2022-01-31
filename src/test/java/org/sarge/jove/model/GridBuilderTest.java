package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.model.GridBuilder.HeightFunction;

class GridBuilderTest {
	private GridBuilder builder;

	@BeforeEach
	void before() {
		builder = new GridBuilder();
	}

	@DisplayName("Create an unindexed grid with duplicate vertices (delegates to the index factory of the primitive")
	@Test
	void build() {
		final Model model = builder.tile(0.25f).build();
		assertNotNull(model);
		assertEquals(Primitive.TRIANGLES, model.primitive());
		assertEquals((3 * 3) * (2 * 3), model.count());
		assertEquals(false, model.isIndexed());
	}

	@DisplayName("Create a simple grid with no index factory applied (points)")
	@Test
	void buildNotIndexed() {
		final MutableModel model = builder
				.primitive(Primitive.POINTS)
				.index(null)
				.build();

		assertNotNull(model);
		assertEquals(4 * 4, model.count());
		assertEquals(false, model.isIndexed());
	}

	@DisplayName("Create a grid with an overridden index factory (patch control points comprising quads)")
	@Test
	void buildQuadStrip() {
		final MutableModel model = builder.primitive(Primitive.PATCH).index(Quad.STRIP).build();
		assertNotNull(model);
		assertEquals((3 * 3) * 4, model.count());
		assertEquals(Primitive.PATCH, model.primitive());
		assertEquals(List.of(Point.LAYOUT, Coordinate2D.LAYOUT), model.layout());
		assertEquals(true, model.isIndexed());
	}

	@DisplayName("Create a grid comprising a triangle strip with degenerate triangles")
	@Test
	void buildTriangleStrip() {
		// TODO
	}

	@Nested
	class HeightFunctionTest {
		@Test
		void literal() {
			final HeightFunction function = HeightFunction.literal(3);
			assertNotNull(function);
			assertEquals(3, function.height(1, 2));
		}

		@Test
		void image() {
			// Create a height-map image
			final ImageData image = mock(ImageData.class);
			when(image.extents()).thenReturn(new ImageData.Extents(new Dimensions(8, 8)));
			when(image.components()).thenReturn("RGBA");
			when(image.layout()).thenReturn(new Layout(1, Byte.class, 2, false));
			when(image.pixel(2, 2, 1)).thenReturn(65535);

			// Create height-map function
			final HeightFunction function = HeightFunction.heightmap(new Dimensions(4, 4), image, 1, 2);
			assertNotNull(function);

			// Check grid coordinates are mapped to the height-map
			assertEquals(2, function.height(1, 1));
		}

		@Test
		void imageInvalidComponentIndex() {
			final ImageData image = mock(ImageData.class);
			when(image.components()).thenReturn(StringUtils.EMPTY);
			assertThrows(IllegalArgumentException.class, () -> HeightFunction.heightmap(new Dimensions(4, 4), image, 0, 1));
		}
	}
}
