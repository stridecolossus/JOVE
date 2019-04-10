package org.sarge.jove.texture;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;

public class HeightMapTest {
	@Nested
	class HeightMapTests {
		private HeightMap map;

		@BeforeEach
		public void before() {
			map = new HeightMap(new int[]{1, 2, 3, 4});
		}

		@Test
		public void constructor() {
			assertEquals(2, map.size());
		}

		@Test
		public void constructorNotSquare() {
			assertThrows(IllegalArgumentException.class, () -> new HeightMap(new int[]{1, 2, 3}));
		}

		@Test
		public void height() {
			assertEquals(1, map.height(0, 0));
			assertEquals(2, map.height(1, 0));
			assertEquals(3, map.height(0, 1));
			assertEquals(4, map.height(1, 1));
		}
	}

	@Nested
	class BuilderTests {
		@Test
		public void builder() {
			final HeightMap map = new HeightMap.Builder(2)
				.set(3)
				.set(0, 0, 4)
				.build();
			assertNotNull(map);
			assertEquals(2, map.size());
			assertEquals(4, map.height(0, 0));
			assertEquals(3, map.height(1, 0));
			assertEquals(3, map.height(0, 1));
			assertEquals(3, map.height(1, 1));
		}
	}

	@Nested
	class ImageTests {
		@Test
		public void image() {
			// Create image buffer
			// TODO - this is nasty!
			final byte zero = (byte) 0;
			final ByteBuffer buffer = ByteBuffer.allocate(2 * 2 * 4);
			for(int n = 0; n < 4; ++n) {
				buffer.put(zero).put(zero).put(zero).put((byte) n);
			}
			buffer.flip();

			// Create image
			final Image image = mock(Image.class);
			when(image.format()).thenReturn(Image.Format.GRAY_SCALE);
			when(image.size()).thenReturn(new Dimensions(2, 2));
			when(image.buffer()).thenReturn(buffer);

			// Create height-map from image
			final HeightMap map = HeightMap.of(image);
			assertNotNull(map);
			assertEquals(2, map.size());
			assertEquals(0, map.height(0, 0));
			assertEquals(1, map.height(1, 0));
			assertEquals(2, map.height(0, 1));
			assertEquals(3, map.height(1, 1));
		}

		@Test
		public void imageNotSquare() {
			final Image image = mock(Image.class);
			when(image.format()).thenReturn(Image.Format.GRAY_SCALE);
			when(image.size()).thenReturn(new Dimensions(2, 3));
			assertThrows(IllegalArgumentException.class, () -> HeightMap.of(image));
		}
	}
}
