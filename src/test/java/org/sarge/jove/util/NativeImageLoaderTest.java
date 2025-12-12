package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.util.ImageData.Level;

class NativeImageLoaderTest {
	private NativeImageLoader loader;

	@BeforeEach
	void before() {
		loader = new NativeImageLoader();
	}

	private static void check(ImageData image) {
		// Check image header
		assertEquals(new Dimensions(2, 3), image.size());
		assertEquals(1, image.layers());
		assertNotNull(image.channels());
		assertEquals(0, image.format());

		// Check layout
		final int num = image.channels().length();
		final Layout layout = image.layout();
		assertNotNull(layout);
		assertEquals(num, layout.count());
		assertEquals(Layout.Type.NORMALIZED, layout.type());
		assertEquals(Byte.BYTES, layout.bytes());
		assertEquals(false, layout.signed());
		assertEquals(num, layout.stride());

		// Check MIP level
		final int len = 2 * 3 * num;
		assertEquals(List.of(new Level(0, len)), image.levels());

		// Check image data
		assertEquals(len, image.data().byteSize());
	}

	@DisplayName("ABGR should be loaded as-is")
	@Test
	void load() throws IOException {
		final BufferedImage buffered = new BufferedImage(2, 3, BufferedImage.TYPE_4BYTE_ABGR);
		final ImageData image = loader.load(buffered);
		assertNotNull(image);
		assertEquals("ABGR", image.channels());
		check(image);
	}

	@DisplayName("Loader should add an alpha channel as required")
	@Test
	void loadAddAlpha() throws IOException {
		final BufferedImage buffered = new BufferedImage(2, 3, BufferedImage.TYPE_3BYTE_BGR);
		final ImageData image = loader.load(buffered);
		assertNotNull(image);
		assertEquals("ABGR", image.channels());
		check(image);
	}

	@DisplayName("Gray-scale buffered should have one channel")
	@Test
	void grayscale() throws IOException {
		final BufferedImage buffered = new BufferedImage(2, 3, BufferedImage.TYPE_BYTE_GRAY);
		final ImageData image = loader.load(buffered);
		assertNotNull(image);
		assertEquals("R", image.channels());
		check(image);
	}

	@DisplayName("Should fail for an unsupported buffered type")
	@Test
	void unsupported() {
		final BufferedImage buffered = new BufferedImage(2, 3, BufferedImage.TYPE_BYTE_BINARY);
		assertThrows(RuntimeException.class, () -> loader.load(buffered));
	}
}
