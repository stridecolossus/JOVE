package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.ImageData.DefaultImageData;
import org.sarge.jove.io.ImageData.Extents;
import org.sarge.jove.io.ImageData.Level;

public class ImageDataTest {
	private static final Extents EXTENTS = new Extents(new Dimensions(2, 3));
	private static final List<Level> LEVELS = List.of(new Level(0, 2 * 3 * 4));
	private static final Layout LAYOUT = Layout.bytes(4, 1);
	private static final byte[] DATA = new byte[2 * 3 * 4];

	@Nested
	class DefaultImageDataTests {
		private ImageData image;
		private IntUnaryOperator pixel;

		@BeforeEach
		void before() {
			pixel = n -> DATA[n];
			image = new DefaultImageData(EXTENTS, "RGBA", LAYOUT, 42, LEVELS, 1, Bufferable.of(DATA), pixel);
		}

		@Test
		void constructor() {
			assertEquals(new Extents(new Dimensions(2, 3)), image.extents());
			assertEquals("RGBA", image.components());
			assertEquals(Layout.bytes(4, 1), image.layout());
			assertEquals(42, image.format());
			assertEquals(1, image.layers());
			assertEquals(List.of(new Level(0, 2 * 3 * 4)), image.levels());
			assertNotNull(image.data());
		}

		@Test
		void invalidComponentLayout() {
			assertThrows(IllegalArgumentException.class, () -> new DefaultImageData(EXTENTS, "RGBA", Layout.bytes(3, 1), 0, LEVELS, 1, Bufferable.of(DATA), pixel));
		}

		@Test
		void invalidDataLength() {
			assertThrows(IllegalArgumentException.class, () -> new DefaultImageData(EXTENTS, "RGBA", LAYOUT, 0, LEVELS, 1, Bufferable.of(new byte[0]), pixel));
		}

		@Test
		void pixel() {
			assertEquals(0, image.pixel(0, 0, 0));
		}

		@Test
		void pixelInvalidCoordinate() {
			assertThrows(ArrayIndexOutOfBoundsException.class, () -> image.pixel(2, 3, 0));
		}

		@Test
		void pixelInvalidComponentIndex() {
			assertThrows(IllegalArgumentException.class, () -> image.pixel(0, 0, 4));
		}
	}

	@Nested
	class LevelTests {
		private Level level;

		@BeforeEach
		void before() {
			level = new Level(3, 16);
		}

		@Test
		void constructor() {
			assertEquals(3, level.offset());
			assertEquals(16, level.length());
		}

		@Test
		void offset() {
			assertEquals(3, level.offset(0, 4));
			assertEquals(7, level.offset(1, 4));
			assertEquals(11, level.offset(2, 4));
			assertEquals(15, level.offset(3, 4));
		}

		@Test
		void offsetInvalidLayerIndex() {
			assertThrows(IllegalArgumentException.class, () -> level.offset(4, 4));
		}
	}

	@Nested
	class ExtentsTests {
		private Extents extents;

		@BeforeEach
		void before() {
			extents = new Extents(new Dimensions(640, 480), 3);
		}

		@Test
		void constructor() {
			assertEquals(3, extents.depth());
			assertEquals(640, extents.size().width());
			assertEquals(480, extents.size().height());
			assertEquals(3, extents.depth());
		}

		@Test
		void mip() {
			assertEquals(new Extents(new Dimensions(320, 240), 3), extents.mip(1));
			assertEquals(extents, extents.mip(0));
		}
	}
}
