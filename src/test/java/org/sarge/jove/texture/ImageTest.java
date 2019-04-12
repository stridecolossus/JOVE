package org.sarge.jove.texture;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.texture.Image.Format;
import org.sarge.jove.texture.Image.Loader;
import org.sarge.jove.texture.Image.Type;

public class ImageTest {
	@Nested
	class ImageTests {
		private Image image;

		@BeforeEach
		public void before() {
			image = new Image(new Format(4, Type.BYTE), new Dimensions(7, 8), ByteBuffer.allocate(7 * 8 * 4));
		}

		@Test
		public void constructor() {
			assertEquals(new Format(4, Type.BYTE), image.format());
			assertEquals(new Dimensions(7, 8), image.size());
		}

		@Test
		public void buffer() {
			final ByteBuffer buffer = image.buffer();
			assertNotNull(buffer);
			assertEquals(0, buffer.position());
			assertEquals(7 * 8 * 4, buffer.capacity());
		}

		@Test
		public void invalidComponentNumber() {
			assertThrows(IllegalArgumentException.class, () -> new Image(new Format(0, Type.BYTE), new Dimensions(7, 8), ByteBuffer.allocate(42)));
			assertThrows(IllegalArgumentException.class, () -> new Image(new Format(5, Type.BYTE), new Dimensions(7, 8), ByteBuffer.allocate(42)));
		}

		@Test
		public void invalidBufferLength() {
			assertThrows(IllegalArgumentException.class, () -> new Image(new Format(4, Type.BYTE), new Dimensions(7, 8), ByteBuffer.allocate(999)));
		}
	}

	@Nested
	class LoaderTests {
		private Loader loader = new Loader();

		@BeforeEach
		public void before() {
			loader = new Loader();
		}

		@Test
		public void RGB() throws IOException {
			final Image image = loader.load(ImageTest.class.getClassLoader().getResourceAsStream("thiswayup.jpg"));
			assertNotNull(image);
			assertEquals(new Format(4, Type.BYTE), image.format());
			assertEquals(new Dimensions(128, 128), image.size());
			assertNotNull(image.buffer());
		}

		@Test
		public void RGBA() throws IOException {
			final Image image = loader.load(ImageTest.class.getClassLoader().getResourceAsStream("chalet.jpg"));
			assertNotNull(image);
			assertEquals(new Format(4, Type.BYTE), image.format());
			assertEquals(new Dimensions(4096, 4096), image.size());
			assertNotNull(image.buffer());
		}

		@Test
		public void GIF() throws IOException {
			final Image image = loader.load(ImageTest.class.getClassLoader().getResourceAsStream("heightmap.gif"));
			assertNotNull(image);
			assertEquals(new Format(1, Type.BYTE), image.format());
			assertEquals(new Dimensions(256, 256), image.size());
			assertNotNull(image.buffer());
		}
	}
}
