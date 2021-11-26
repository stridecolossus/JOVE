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
import org.sarge.jove.io.ImageData.Level;

public class ImageDataTest {
	@Nested
	class AbstractImageDataTests {
		class MockImageData extends AbstractImageData {
			public MockImageData(Layout layout, Level level) {
				super(new Dimensions(2, 3), "RGBA", layout, List.of(level));
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
			assertEquals(new Dimensions(2, 3), image.size());
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
