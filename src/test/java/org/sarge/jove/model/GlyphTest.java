package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.*;

class GlyphTest {
	private Glyph glyph;
	private Glyph kerning;

	@BeforeEach
	void before() {
		glyph = new Glyph(3);
		kerning = glyph.kerning(Map.of('W', 4f));
	}

	@Test
	void advance() {
		assertEquals(3f, glyph.advance());
		assertEquals(3f, glyph.advance('W'));
	}

	@Test
	void kerning() {
		assertEquals(3f, kerning.advance());
		assertEquals(3f, kerning.advance('?'));
		assertEquals(4f, kerning.advance('W'));
	}

	@Test
	void write() {
		assertEquals(Map.of("advance", 3f), glyph.write());
		assertEquals(Map.of("advance", 3f, "kerning", Map.of('W', 4f)), kerning.write());
	}
}
