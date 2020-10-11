package org.sarge.jove.texture;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
			final Image image = new Image(new Image.Format(1, Image.Type.BYTE), new Dimensions(3, 3), ByteBuffer.allocate(3 * 3));
			final HeightMap map = HeightMap.format(image);
			assertNotNull(map);
			assertEquals(3, map.size());
		}

		@Test
		public void imageNotSquare() {
			final Image image = new Image(new Image.Format(1, Image.Type.BYTE), new Dimensions(3, 4), ByteBuffer.allocate(3 * 4));
			assertThrows(IllegalArgumentException.class, () -> HeightMap.format(image));
		}

		@Test
		public void imageInvalidFormat() {
			final Image image = new Image(new Image.Format(2, Image.Type.BYTE), new Dimensions(3, 4), ByteBuffer.allocate(3 * 4 * 2));
			assertThrows(IllegalArgumentException.class, () -> HeightMap.format(image));
		}
	}
}
