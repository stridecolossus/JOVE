package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.model.GlyphFont.Loader;
import org.sarge.lib.element.Element;

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

	@Nested
	class LoaderTests {
		private Loader loader;

		@BeforeEach
		void before() {
			loader = new Loader();
		}

		@Test
		void load() throws Exception {
			final Element root = new Element.Builder()
					.child("start", (int) 'A')
					.child("tiles", 16)
					.child()
						.name("glyphs")
						.child()
							.child("code", (int) 'A')
							.child("advance", 3)
							.child()
								.name("kerning")
								.child(String.valueOf((int) 'W'), "4")
								.end()
							.end()
						.end()
					.build();

			assertEquals(font, loader.load(root));
		}

		@Test
		void write() throws Exception {
			// Output font
			final StringWriter out = new StringWriter();
			Loader.write(font, out);

			// Load back in and validate
			final var in = new ByteArrayInputStream(out.toString().getBytes());
			final Element data = loader.map(in);
			assertEquals(font, loader.load(data));
		}
	}
}
