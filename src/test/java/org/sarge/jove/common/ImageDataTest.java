package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sarge.jove.common.ImageData.Loader;

public class ImageDataTest {
	private Loader loader;

	@BeforeEach
	void before() {
		loader = new Loader();
	}

	@DisplayName("ABGR should be loaded as-is")
	@Test
	void load() {
		final BufferedImage image = new BufferedImage(1, 2, BufferedImage.TYPE_4BYTE_ABGR);
		final ImageData data = loader.load(image);
		assertNotNull(data);
		assertEquals(new Dimensions(1, 2), data.size());
		assertEquals(new Layout(4, Byte.class, 1, false), data.layout());
		assertEquals("ABGR", data.mapping());
		assertArrayEquals(new byte[1 * 2 * 4], data.bytes());
	}

	@DisplayName("Loader should add an alpha channel as required")
	@Test
	void loadAddAlpha() {
		final BufferedImage image = new BufferedImage(1, 2, BufferedImage.TYPE_3BYTE_BGR);
		final ImageData data = loader.load(image);
		assertNotNull(data);
		assertEquals(new Dimensions(1, 2), data.size());
		assertEquals(new Layout(4, Byte.class, 1, false), data.layout());
		assertEquals("ABGR", data.mapping());
	}

	@DisplayName("Gray-scale image should have one channel")
	@Test
	void grayscale() {
		final BufferedImage image = new BufferedImage(1, 2, BufferedImage.TYPE_BYTE_GRAY);
		final ImageData data = loader.load(image);
		assertNotNull(data);
		assertEquals(new Dimensions(1, 2), data.size());
		assertEquals(new Layout(1, Byte.class, 1, false), data.layout());
		assertEquals("RRR1", data.mapping());
		assertArrayEquals(new byte[1 * 2], data.bytes());
	}

	@DisplayName("Should fail for an unsupported image type")
	@Test
	void unsupported() {
		final BufferedImage image = new BufferedImage(1, 2, BufferedImage.TYPE_BYTE_BINARY);
		assertThrows(RuntimeException.class, () -> loader.load(image));
	}

	@DisplayName("Should load supported image formats")
	@ParameterizedTest
	@CsvSource({
		"duke.jpg, 5",
		"duke.png, 13",
		"heightmap.jpg, 10",
	})
	void map(String filename, int type) throws IOException {
		final Path path = Paths.get("./src/test/resources", filename);
		final BufferedImage image = loader.map(Files.newInputStream(path));
		assertEquals(type, image.getType());
		assertNotNull(loader.load(image));
	}
}
