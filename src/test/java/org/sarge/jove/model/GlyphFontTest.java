package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

class GlyphFontTest {
	private GlyphFont font;
	private Glyph glyph;

	@BeforeEach
	void before() {
		glyph = new Glyph('A', 3, Map.of((int) 'W', 4f));
		font = new GlyphFont('A', List.of(glyph), 16);
	}

	@Test
	void constructor() {
		assertEquals(1, font.glyphs());
		assertEquals('A', font.start());
		assertEquals(16, font.tiles());
	}

	@DisplayName("A glyph can be retrieved by character")
	@Test
	void glyph() {
		assertEquals(glyph, font.glyph('A'));
	}

	@DisplayName("A glyph index must be within the available character range")
	@Test
	void bounds() {
		assertThrows(IndexOutOfBoundsException.class, () -> font.glyph((char) 31));
		assertThrows(IndexOutOfBoundsException.class, () -> font.glyph((char) 999));
	}

	@DisplayName("The number of glyphs must fit within the texture tiles")
	@Test
	void tiles() {
		final var glyphs = Collections.nCopies(256 + 1, glyph);
		assertThrows(IllegalStateException.class, () -> new GlyphFont(0, glyphs, 16));
	}
}
