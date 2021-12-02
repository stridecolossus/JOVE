package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;

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
	@Nested
	class DefaultImageDataTests {
		private ImageData image;

		@BeforeEach
		void before() {
			image = new DefaultImageData(new Extents(new Dimensions(2, 3)), "RGBA", Layout.bytes(4), 42, List.of(new Level(1, 2)), 1, mock(Bufferable.class));
		}

		@Test
		void constructor() {
			assertEquals(new Extents(new Dimensions(2, 3)), image.extents());
			assertEquals("RGBA", image.components());
			assertEquals(Layout.bytes(4), image.layout());
			assertEquals(42, image.format());
			assertEquals(1, image.layers());
			assertEquals(List.of(new Level(1, 2)), image.levels());
			assertNotNull(image.data());
		}

		@Test
		void invalidComponentLayout() {
			assertThrows(IllegalArgumentException.class, () -> new DefaultImageData(new Extents(new Dimensions(2, 3)), "RGBA", Layout.bytes(3), 42, List.of(new Level(1, 2)), 1, mock(Bufferable.class)));
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
