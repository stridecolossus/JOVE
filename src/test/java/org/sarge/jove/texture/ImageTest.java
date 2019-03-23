package org.sarge.jove.texture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.texture.Image.Format;

public class ImageTest {
	@Test
	public void header() {
		final Image.Header header = new Image.Header(Format.RGBA, new Dimensions(3, 4));
		assertEquals(Format.RGBA, header.format());
		assertEquals(new Dimensions(3, 4), header.size());
	}

	@Test
	public void isTranslucent() {
		assertEquals(true, Format.RGBA.isTranslucent());
		assertEquals(false, Format.RGB.isTranslucent());
		assertEquals(false, Format.GRAY_SCALE.isTranslucent());
	}
}
