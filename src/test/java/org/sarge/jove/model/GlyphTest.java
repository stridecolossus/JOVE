package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.*;

class GlyphTest {
	private Glyph glyph;

	@BeforeEach
	void before() {
		glyph = new Glyph(3, Map.of('W', 4f));
	}

	@DisplayName("A glyph has a character advance")
	@Test
	void advance() {
		assertEquals(3f, glyph.advance());
	}

	@DisplayName("A glyph has a character advance to the next character")
	@Test
	void next() {
		assertEquals(3f, glyph.advance('?'));
	}

	@DisplayName("A glyph can override the character advance for kerning pairs")
	@Test
	void kerning() {
		assertEquals(4f, glyph.advance('W'));
	}
}
