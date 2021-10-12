package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sarge.jove.common.ImageData.Loader;

public class ImageDataTest {
	private static final int LENGTH = 4 * (3 * 4);

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

			// Check image properties
			assertEquals(new Dimensions(w, h), image.size());
			assertEquals(new Layout(components, Byte.class, false), image.layout());

			// Check image data
			assertNotNull(image.bytes());
			assertEquals(w * h * components, image.bytes().length);
		}

		@Test
		void loadUnsupportedFormat() throws IOException {
			final BufferedImage wrong = new BufferedImage(1, 2, BufferedImage.TYPE_USHORT_555_RGB);
			assertThrows(RuntimeException.class, () -> loader.load(wrong));
		}
	}
}
