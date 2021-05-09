package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sarge.jove.common.ImageData.DefaultImageData;
import org.sarge.jove.common.ImageData.Loader;

public class ImageDataTest {
	private Dimensions size;
	private List<Integer> components;

	@BeforeEach
	void before() {
		size = new Dimensions(3, 4);
		components = List.of(8, 8, 8);
	}

	@Test
	void constructor() {
		final int len = 3 * size.width() * size.height();
		final ImageData image = new DefaultImageData(size, components, new byte[len]);
		assertEquals(size, image.size());
		assertEquals(components, image.components());
	}

	@Test
	void constructorInvalidArrayLength() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultImageData(size, components, new byte[0]));
		assertThrows(IllegalArgumentException.class, () -> new DefaultImageData(size, components, new byte[42]));
	}

	@Test
	void constructorEmptyComponents() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultImageData(size, List.of(), new byte[3 * 3 * 4]));
	}

	@Nested
	class LoaderTests {
		private Loader loader;

		@BeforeEach
		void before() {
			loader = new Loader();
		}

		@SuppressWarnings("resource")
		@ParameterizedTest
		@CsvSource({
			"duke.jpg, 375, 375, 4",
			"duke.png, 375, 375, 4",
			"heightmap.jpg, 256, 256, 1",
		})
		void load(String filename, int w, int h, int components) throws IOException {
			// Load image from file-system
			final Path path = Paths.get("./src/test/resources", filename);
			final BufferedImage buffered = ImageIO.read(Files.newInputStream(path));

			// Load image wrapper
			final ImageData image = loader.load(buffered);
			assertNotNull(image);
			assertEquals(new Dimensions(w, h), image.size());
			assertNotNull(image.components());
			assertEquals(components, image.components().size());
			assertNotNull(image.data());
		}

		@Test
		void loadUnsupportedFormat() throws IOException {
			final BufferedImage wrong = new BufferedImage(1, 2, BufferedImage.TYPE_USHORT_555_RGB);
			assertThrows(RuntimeException.class, () -> loader.load(wrong));
		}
	}
}
