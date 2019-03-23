package org.sarge.jove.texture;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.texture.Image.Format;

public class DefaultImageTest {
	private DefaultImage image;

	@BeforeEach
	public void before() {
		image = new DefaultImage(new BufferedImage(3, 4, BufferedImage.TYPE_4BYTE_ABGR));
	}

	@Test
	public void descriptor() {
		final Image.Header expected = new Image.Header(Format.RGBA, new Dimensions(3, 4));
		assertEquals(expected, image.header());
	}

	@Test
	public void pixel() {
		image.pixel(0, 0);
	}

	@Test
	public void buffer() {
		final ByteBuffer buffer = image.buffer();
		assertNotNull(buffer);
		assertEquals(0, buffer.position());
		assertEquals(4 * 3 * 4, buffer.capacity());
	}

	@Test
	public void convert() {
		final Image.Header header = new Image.Header(Format.RGB, new Dimensions(6, 8));
		final Image result = image.convert(header);
		assertNotNull(result);
		assertEquals(header, result.header());
	}

	@Test
	public void load() throws IOException {
		// Load image
		final DefaultImage.Loader loader = new DefaultImage.Loader();
		final Image image = loader.load(DefaultImageTest.class.getClassLoader().getResourceAsStream("thiswayup.jpg"));
		assertNotNull(image);

		// Check image format
		final Image.Header expected = new Image.Header(Format.RGB, new Dimensions(128, 128));
		assertEquals(expected, image.header());
	}
}
