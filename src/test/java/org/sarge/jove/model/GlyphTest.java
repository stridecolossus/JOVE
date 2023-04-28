package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.*;

class GlyphTest {
	private Glyph glyph;

	@BeforeEach
	void before() {
		glyph = new Glyph(1, 2, Map.of(3, 4f));
	}

	@DisplayName("A glyph has a character advance")
	@Test
	void advance() {
		assertEquals(2f, glyph.advance());
		assertEquals(2f, glyph.advance('?'));
	}

	@DisplayName("A glyph can override the character advance for kerning pairs")
	@Test
	void kerning() {
		assertEquals(4f, glyph.advance((char) 3));
	}
}
