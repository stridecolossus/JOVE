package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;

public class ImageDataTest {
	private ImageData image;
	private Layout layout;

	@BeforeEach
	void before() {
		layout = new Layout("RGBA", Byte.class, 1, true);
		image = new ImageData(new Dimensions(2, 3), 1, 1, layout, Bufferable.of(new byte[2 * 3 * 4]));
	}

	@Test
	void constructor() {
		assertEquals(new Dimensions(2, 3), image.size());
		assertEquals(1, image.count());
		assertEquals(1, image.mip());
		assertEquals(layout, image.layout());
		assertNotNull(image.data());
		assertEquals(2 * 3 * 4, image.data().length());
	}

	@Test
	void constructorInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> new ImageData(new Dimensions(2, 3), 1, 1, layout, Bufferable.of(new byte[0])));
	}

	@Test
	void offset() {
		assertEquals(0, image.offset(0));
	}

	@Test
	void offsetInvalidIndex() {
		assertThrows(IllegalArgumentException.class, () -> image.offset(1));
	}

	@Test
	void array() {
		// Create image array
		final ImageData array = ImageData.array(List.of(image, image));
		assertNotNull(array);

		// Check header
		assertEquals(new Dimensions(2, 3), array.size());
		assertEquals(2, array.count());
		assertEquals(1, array.mip());
		assertEquals(layout, array.layout());

		// Check compound image data
		final Bufferable data = array.data();
		assertNotNull(data);
		assertEquals(2 * (2 * 3 * 4), data.length());

		// Check offsets
		assertEquals(0, array.offset(0));
		assertEquals(2 * 3 * 4, array.offset(1));
	}

	@Test
	void arrayEmptyArray() {
		assertThrows(IllegalArgumentException.class, () -> ImageData.array(List.of()));
	}

	@Test
	void arrayMismatchedImage() {
		final ImageData other = new ImageData(new Dimensions(3, 2), 1, 1, layout, Bufferable.of(new byte[2 * 3 * 4]));
		assertThrows(IllegalArgumentException.class, () -> ImageData.array(List.of(image, other)));
	}

	@Test
	void arrayAlreadyImageArray() {
		final ImageData array = new ImageData(new Dimensions(3, 2), 2, 1, layout, Bufferable.of(new byte[2 * 2 * 3 * 4]));
		assertThrows(IllegalArgumentException.class, () -> ImageData.array(List.of(array)));
	}
}
