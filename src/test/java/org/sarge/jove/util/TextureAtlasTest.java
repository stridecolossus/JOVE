package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.util.TextureAtlas.Loader;

public class TextureAtlasTest {
	private TextureAtlas atlas;

	@BeforeEach
	void before() {
		atlas = new TextureAtlas(Map.of("name", new Rectangle(1, 2, 3, 4)));
	}

	@Test
	void constructor() {
		assertEquals(1, atlas.size());
		assertEquals(new Rectangle(1, 2, 3, 4), atlas.get("name"));
	}

	@Test
	void cubemap() {
		// Create sub-map
		final Dimensions size = new Dimensions(2, 3);
		final TextureAtlas cubemap = TextureAtlas.cubemap(size);
		assertNotNull(cubemap);
		assertEquals(6, cubemap.size());

		// Check key order
		final String[] keys = {"+X", "-X", "+Y", "-Y", "+Z", "-Z"};
		assertArrayEquals(keys, cubemap.keySet().toArray());

		// Check rectangles
		assertEquals(new Rectangle(2 * 2, 1 * 3, size), cubemap.get("+X"));
		assertEquals(new Rectangle(0 * 2, 1 * 3, size), cubemap.get("-X"));
		assertEquals(new Rectangle(1 * 2, 0 * 3, size), cubemap.get("+Y"));
		assertEquals(new Rectangle(1 * 2, 2 * 3, size), cubemap.get("-Y"));
		assertEquals(new Rectangle(1 * 2, 1 * 3, size), cubemap.get("+Z"));
		assertEquals(new Rectangle(3 * 2, 1 * 3, size), cubemap.get("-Z"));
	}

	@Nested
	class LoaderTests {
		private Loader loader;

		@BeforeEach
		void before() {
			loader = new Loader();
		}

		@Test
		void load() throws IOException {
			// Create JSON
			final String json = """
					{
						atlas: [
							{
								name: one,
								rect: [1, 2, 3, 4]
							},
							{
								name: two,
								rect: [1, 2, 3, 4]
							}
						]
					}
			""";

			// Load JSON
			final JSONObject obj = new JSONObject(new JSONTokener(new StringReader(json)));

			// Load texture atlas
			final TextureAtlas atlas = loader.load(obj);
			assertNotNull(atlas);
			assertEquals(2, atlas.size());

			// Check atlas
			final Rectangle rect = new Rectangle(1, 2, 3, 4);
			assertEquals(rect, atlas.get("one"));
			assertEquals(rect, atlas.get("two"));
		}
	}
}
