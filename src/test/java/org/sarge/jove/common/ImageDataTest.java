package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.ImageData.DefaultImageData;
import org.sarge.jove.common.ImageData.Loader;
import org.sarge.jove.util.DataSource;

public class ImageDataTest {
	private Dimensions size;

	@BeforeEach
	void before() {
		size = new Dimensions(3, 4);
	}

	@Test
	void constructor() {
		final int len = 3 * 4 * 4;
		final ImageData image = new DefaultImageData(size, new byte[len]);
		assertEquals(size, image.size());
		assertNotNull(image.buffer());
		assertEquals(len, image.buffer().capacity());
	}

	@Test
	void constructorInvalidArrayLength() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultImageData(size, new byte[42]));
	}

	@Nested
	class LoaderTests {
		private Loader loader;

		@BeforeEach
		void before() {
			loader = new Loader(DataSource.file(new File("./src/test/resources")));
		}

		@Test
		void load() throws IOException {
			final ImageData image = loader.load("statue.jpg");
			assertNotNull(image);
			assertEquals(new Dimensions(512, 512), image.size());
			assertNotNull(image.buffer());
		}

		// TODO
		// - image with alpha
		// - unknown image
		// - unsupported format
	}
}
