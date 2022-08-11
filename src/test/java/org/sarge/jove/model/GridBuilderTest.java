package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.model.GridBuilder.HeightFunction;
import org.sarge.jove.model.Model.Header;

class GridBuilderTest {
	private GridBuilder builder;

	@BeforeEach
	void before() {
		builder = new GridBuilder();
	}

	@DisplayName("Create a grid with an overridden index factory (patch control points comprising quads)")
	@Test
	void buildQuadStrip() {
		final Model model = builder.primitive(Primitive.PATCH).index(IndexFactory.QUADS).build();
		assertNotNull(model);
		assertEquals(new Header(Primitive.PATCH, (3 * 3) * 4, List.of(Point.LAYOUT, Coordinate2D.LAYOUT)), model.header());
		assertTrue(model.index().isPresent());
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
			when(image.size()).thenReturn(new Dimensions(8, 8));
			when(image.components()).thenReturn("RGBA");
			when(image.layout()).thenReturn(new Layout(1, Layout.Type.INTEGER, 2, false));
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
