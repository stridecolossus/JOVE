package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.ImageData.DefaultImageData;
import org.sarge.jove.common.ImageData.Loader;
import org.sarge.jove.common.ImageData.Swizzle;
import org.sarge.jove.util.DataSource;

public class ImageDataTest {
	private Dimensions size;
	private int[] components;

	@BeforeEach
	void before() {
		size = new Dimensions(3, 4);
		components = new int[]{8, 8, 8};
	}

	@Test
	void constructor() {
		final int len = 3 * (3 * 4);
		final ImageData image = new DefaultImageData(size, components, ByteBuffer.allocate(len));
		assertEquals(size, image.size());
		assertNotNull(image.buffer());
		assertEquals(len, image.buffer().capacity());
		assertEquals(true, image.buffer().isReadOnly());
	}

	@Test
	void constructorInvalidArrayLength() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultImageData(size, components, ByteBuffer.allocate(42)));
	}

	@Test
	void constructorEmptyComponents() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultImageData(size, new int[]{}, ByteBuffer.allocate(42)));
	}

	@Nested
	class ConverterTests {
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
			swizzle.transform(bytes, 3);
			assertArrayEquals(new byte[]{2, 1, 0, 5, 4, 3}, bytes);
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
			// Load image from file-system
			final ImageData image = loader.load("statue.jpg");

			// Check image
			assertNotNull(image);
			assertEquals(new Dimensions(512, 512), image.size());
			assertNotNull(image.components());
			assertEquals(4, image.components().size());

			// Check buffer
			assertNotNull(image.buffer());
			assertTrue(image.buffer().isReadOnly());
			assertEquals(512 * 512 * 4, image.buffer().capacity());
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
