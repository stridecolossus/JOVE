package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.ImageData.DefaultImageData;
import org.sarge.jove.common.ImageData.Loader;
import org.sarge.jove.common.ImageData.Loader.Swizzle;
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
		final ImageData image = new DefaultImageData(size, ByteBuffer.allocate(len));
		assertEquals(size, image.size());
		assertNotNull(image.buffer());
		assertEquals(len, image.buffer().capacity());
		assertEquals(true, image.buffer().isReadOnly());
	}

	@Test
	void constructorInvalidArrayLength() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultImageData(size, ByteBuffer.allocate(42)));
	}

	@Nested
	class TransformerTests {
		private byte[] bytes;

		@BeforeEach
		void before() {
			bytes = new byte[6];
			for(int n = 0; n < bytes.length; ++n) {
				bytes[n] = (byte) n;
			}
		}

		@Test
		void swizzle() {
			final Swizzle swizzle = new Swizzle(0, 2);
			swizzle.apply(bytes, 3);
			assertArrayEquals(new byte[]{2, 1, 0, 5, 4, 3}, bytes);
		}

		@Test
		void alpha() {
			// Add alpha channel
			final ByteBuffer bb = ImageData.Loader.alpha(bytes, 3, (byte) 42);
			assertNotNull(bb);
			assertEquals(8, bb.capacity());
			bb.flip();

			// Check alpha values injected
			final byte[] result = new byte[8];
			bb.get(result);
			assertArrayEquals(new byte[]{0, 1, 2, 42, 3, 4, 5, 42}, result);
		}
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

		@Test
		void loadUnsupportedFormat() throws IOException {

		}

		// TODO
		// - image with alpha
		// - unknown image
		// - unsupported format
	}
}
