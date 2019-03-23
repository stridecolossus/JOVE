package org.sarge.jove.texture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.texture.TextureAtlas.Loader;

public class TextureAtlasTest {
	private static final Rectangle RECTANGLE = new Rectangle(1, 2, 3, 4);

	private TextureAtlas atlas;

	@BeforeEach
	public void before() {
		atlas = new TextureAtlas(new Dimensions(5, 6), Map.of("name", RECTANGLE));
	}

	@Test
	public void constructor() {
		assertEquals(new Dimensions(5, 6), atlas.size());
	}

	@Test
	public void atlas() {
		assertEquals(Map.of("name", RECTANGLE), atlas.atlas());
	}

	@Test
	public void get() {
		assertEquals(RECTANGLE, atlas.get("name"));
	}

	@Test
	public void getNotFound() {
		assertThrows(IllegalArgumentException.class, () -> atlas.get("cobblers"));
	}

	@Test
	public void load() throws IOException {
		// Load texture atlas
		final Loader loader = new Loader();
		final TextureAtlas atlas = loader.load(new InputStreamReader(TextureAtlasTest.class.getClassLoader().getResourceAsStream("atlas.xml")));
		assertNotNull(atlas);
		assertEquals(new Dimensions(512, 768), atlas.size());

		// Check meta-data
		final Map<String, Rectangle> expected = Map.of(
		    "heightmap.gif", new Rectangle(128, 0, 256, 256),
		    "statue.jpg", new Rectangle(0, 256, 512, 512),
		    "thiswayup.jpg", new Rectangle(0, 0, 128, 128)
		);
		assertEquals(expected, atlas.atlas());
	}
}
