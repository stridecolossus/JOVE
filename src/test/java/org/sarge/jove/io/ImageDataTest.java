package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.ImageData.AbstractImageData;

public class ImageDataTest {
	@Nested
	class AbstractImageDataTests {
		class MockImageData extends AbstractImageData {
			public MockImageData(Dimensions size, String components, Layout layout) {
				super(size, components, layout);
			}

			@Override
			public int levels() {
				return 1;
			}

			@Override
			public Bufferable data(int layer, int level) {
				return null;
			}
		}

		private ImageData image;

		@BeforeEach
		void before() {
			image = new MockImageData(new Dimensions(2, 3), "RGBA", Layout.bytes(4));
		}

		@Test
		void constructor() {
			assertEquals(new Dimensions(2, 3), image.size());
			assertEquals("RGBA", image.components());
			assertEquals(Layout.bytes(4), image.layout());
			assertEquals(1, image.layers());
		}

		@Test
		void invalidComponentLayout() {
			assertThrows(IllegalArgumentException.class, () -> new MockImageData(new Dimensions(2, 3), "RGBA", Layout.bytes(3)));
		}
	}
}
