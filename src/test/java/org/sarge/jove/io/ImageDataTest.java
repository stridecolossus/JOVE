package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.io.ImageData.Level;

public class ImageDataTest {
	private static final Dimensions EXTENTS = new Dimensions(2, 3);
	private static final Layout LAYOUT = new Layout(4, Layout.Type.INTEGER, false, 1);
	private static final byte[] DATA = new byte[2 * 3 * 4];

	private ImageData image;

	@BeforeEach
	void before() {
		image = new ImageData(EXTENTS, "RGBA", LAYOUT, DATA);
	}

	@DisplayName("An image has a header and pixel data")
	@Test
	void constructor() {
		assertEquals(EXTENTS, image.size());
		assertEquals("RGBA", image.channels());
		assertNotNull(image.data());
	}

	@DisplayName("An image has a component layout specifying the structure of each pixel")
	@Test
	void layout() {
		assertEquals(LAYOUT, image.layout());
	}

	@DisplayName("A basic image...")
	@Nested
	class BasicImageTests {
		@DisplayName("has a single MIP level")
		@Test
		void levels() {
			assertEquals(List.of(new Level(0, 2 * 3 * 4)), image.levels());
		}

		@DisplayName("has a single array layer")
		@Test
		void defaults() {
			assertEquals(1, image.depth());
			assertEquals(1, image.layers());
		}

		@DisplayName("does not have a Vulkan format hint")
		@Test
		void format() {
			assertEquals(0, image.format());
		}
	}

	@DisplayName("The layout of an image must match the components string")
	@Test
	void invalidChannel() {
		assertThrows(IllegalArgumentException.class, () -> new ImageData(EXTENTS, "XXX", Layout.floats(3), DATA));
	}

	@DisplayName("The number of channels of an image must match the layout")
	@Test
	void invalidComponentLayout() {
		assertThrows(IllegalArgumentException.class, () -> new ImageData(EXTENTS, "RGBA", Layout.floats(3), DATA));
	}

	@DisplayName("The length of the image data must match the specified layout and dimensions")
	@Test
	void invalidDataLength() {
		assertThrows(IllegalArgumentException.class, () -> new ImageData(EXTENTS, "RGBA", LAYOUT, new byte[0]));
	}

	@DisplayName("An image MIP level...")
	@Nested
	class LevelTests {
		private Level level;

		@BeforeEach
		void before() {
			level = new Level(3, 16);
		}

		@DisplayName("has an offset into the image data and a length")
		@Test
		void constructor() {
			assertEquals(3, level.offset());
			assertEquals(16, level.length());
		}

		@DisplayName("can determine the offset into the image data for a given layer")
		@Test
		void offset() {
			assertEquals(3, level.offset(0, 4));
			assertEquals(7, level.offset(1, 4));
			assertEquals(11, level.offset(2, 4));
			assertEquals(15, level.offset(3, 4));
		}

		@DisplayName("cannot have an offset for an invalid layer")
		@Test
		void offsetInvalidLayerIndex() {
			assertThrows(IllegalArgumentException.class, () -> level.offset(4, 4));
		}
	}

	@DisplayName("A pixel in the image data...")
	@Nested
	class PixelTests {
		@DisplayName("can be looked up by position and channel index")
		@Test
		void pixel() {
			final int index = (1 + 2 * 2) * 4 + 3;
			DATA[index] = 42;
			assertEquals(42, image.pixel(1, 2, 3));
		}

		@DisplayName("cannot be out-of-bounds of the image dimensions")
		@Test
		void pixelInvalidCoordinate() {
			assertThrows(ArrayIndexOutOfBoundsException.class, () -> image.pixel(2, 3, 0));
		}

		@DisplayName("cannot be looked using an invalid channel index")
		@Test
		void pixelInvalidComponentIndex() {
			assertThrows(IllegalArgumentException.class, () -> image.pixel(0, 0, 4));
		}
	}
}
