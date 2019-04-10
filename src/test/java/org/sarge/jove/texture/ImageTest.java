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

public class ImageTest {
	private Image image;

	@BeforeEach
	public void before() {
		image = new Image(Format.RGBA, new Dimensions(3, 4), new BufferedImage(3, 4, BufferedImage.TYPE_4BYTE_ABGR));
	}

	@Test
	public void constructor() {
		assertEquals(Format.RGBA, image.format());
		assertEquals(new Dimensions(3, 4), image.size());
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
	public void loader() throws IOException {
		final Image.Loader loader = new Image.Loader();
		final Image image = loader.load(ImageTest.class.getClassLoader().getResourceAsStream("thiswayup.jpg"));
		assertNotNull(image);
		assertEquals(Format.RGBA, image.format());
		assertEquals(new Dimensions(128, 128), image.size());
	}
}
