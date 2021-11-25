package org.sarge.jove.io;

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
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;

public class NativeImageLoaderTest {
	private NativeImageLoader loader;

	@BeforeEach
	void before() {
		loader = new NativeImageLoader();
	}

	private void check(ImageData image) {
		// Check image header
		assertEquals(new Dimensions(2, 3), image.size());
		assertEquals(1, image.layers());
		assertEquals(1, image.levels());
		assertNotNull(image.components());

		// Check layout
		final int num = image.components().length();
		final Layout layout = image.layout();
		assertNotNull(layout);
		assertEquals(num, layout.size());
		assertEquals(Byte.class, layout.type());
		assertEquals(Byte.BYTES, layout.bytes());
		assertEquals(false, layout.signed());
		assertEquals(num, layout.length());

		// Check image data
		final Bufferable data = image.data(0, 0);
		assertNotNull(data);
		assertEquals(2 * 3 * num, data.length());
	}

	@DisplayName("ABGR should be loaded as-is")
	@Test
	void load() throws IOException {
		final BufferedImage buffered = new BufferedImage(2, 3, BufferedImage.TYPE_4BYTE_ABGR);
		final ImageData image = loader.load(buffered);
		assertNotNull(image);
		assertEquals("ABGR", image.components());
		check(image);
	}

	@DisplayName("Loader should add an alpha channel as required")
	@Test
	void loadAddAlpha() throws IOException {
		final BufferedImage buffered = new BufferedImage(2, 3, BufferedImage.TYPE_3BYTE_BGR);
		final ImageData image = loader.load(buffered);
		assertNotNull(image);
		assertEquals("ABGR", image.components());
		check(image);
	}

	@DisplayName("Gray-scale buffered should have one channel")
	@Test
	void grayscale() throws IOException {
		final BufferedImage buffered = new BufferedImage(2, 3, BufferedImage.TYPE_BYTE_GRAY);
		final ImageData image = loader.load(buffered);
		assertNotNull(image);
		assertEquals("R", image.components());
		check(image);
	}

	@DisplayName("Should fail for an unsupported buffered type")
	@Test
	void unsupported() {
		final BufferedImage buffered = new BufferedImage(2, 3, BufferedImage.TYPE_BYTE_BINARY);
		assertThrows(RuntimeException.class, () -> loader.load(buffered));
	}

	@DisplayName("Should load supported buffered formats")
	@ParameterizedTest
	@CsvSource({
		"duke.jpg, 5",
		//"duke.png, 13",		// TODO - only has 2 channels?
		"heightmap.jpg, 10",
	})
	void map(String filename, int type) throws IOException {
		final Path path = Paths.get("./src/test/resources", filename);
		final BufferedImage buffered = loader.map(Files.newInputStream(path));
		assertEquals(type, buffered.getType());
		assertNotNull(loader.load(buffered));
	}
}
