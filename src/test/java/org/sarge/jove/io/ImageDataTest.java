package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.ImageData.AbstractImageData;
import org.sarge.jove.io.ImageData.Extents;
import org.sarge.jove.io.ImageData.Level;

public class ImageDataTest {
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

	@Nested
	class AbstractImageDataTests {
		class MockImageData extends AbstractImageData {
			public MockImageData(Layout layout, Level level) {
				super(new Extents(new Dimensions(2, 3)), "RGBA", layout, List.of(level));
			}

			@Override
			public Bufferable data(int layer) {
				return null;
			}
		}

		private ImageData image;

		@BeforeEach
		void before() {
			image = new MockImageData(Layout.bytes(4), new Level(0, 2 * 3 * 4));
		}

		@Test
		void constructor() {
			assertEquals(new Extents(new Dimensions(2, 3)), image.extents());
			assertEquals("RGBA", image.components());
			assertEquals(Layout.bytes(4), image.layout());
			assertEquals(1, image.layers());
			assertEquals(List.of(new Level(0, 2 * 3 * 4)), image.levels());
		}

		@Test
		void invalidComponentLayout() {
			assertThrows(IllegalArgumentException.class, () -> new MockImageData(Layout.bytes(3), new Level(0, 1)));
		}
	}
}
