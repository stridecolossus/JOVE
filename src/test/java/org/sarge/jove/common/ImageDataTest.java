package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sarge.jove.common.ImageData.DefaultImageData;
import org.sarge.jove.common.ImageData.Loader;

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
	class LoaderTests {
		private Loader loader;

		@BeforeEach
		void before() {
			loader = new Loader();
		}

		@ParameterizedTest
		@CsvSource({
			"duke.jpg, 375, 375, 4",
			"duke.png, 375, 375, 4",
			"heightmap.jpg, 256, 256, 1",
		})
		void load(String filename, int w, int h, int components) throws IOException {
			// Load image from file-system
			final ImageData image;
			final Path path = Paths.get("./src/test/resources", filename);
			System.out.println(path);
			try(final InputStream in = Files.newInputStream(path)) {
				image = loader.load(in);
			}

			// Check image
			assertNotNull(image);
			assertEquals(new Dimensions(w, h), image.size());
			assertNotNull(image.components());
			assertEquals(components, image.components().size());

			// Check buffer
			assertNotNull(image.buffer());
			assertTrue(image.buffer().isReadOnly());
			assertEquals(w * h * components, image.buffer().capacity());
		}

		@Test
		void loadUnsupportedFormat() throws IOException {
			assertThrows(RuntimeException.class, () -> loader.load(new ByteArrayInputStream(new byte[]{})));
		}
	}
}
