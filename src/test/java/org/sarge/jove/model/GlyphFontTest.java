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
		glyph = new Glyph(2, Map.of('W', 3f));
		font = new GlyphFont(4, List.of(glyph), 5);
	}

	@Test
	void constructor() {
		assertEquals(1, font.glyphs());
		assertEquals(4, font.start());
		assertEquals(5, font.tiles());
	}

	@DisplayName("A glyph can be retrieved by character")
	@Test
	void glyph() {
		assertEquals(glyph, font.glyph((char) 4));
	}

	@DisplayName("A glyph index must be within the available character range")
	@Test
	void bounds() {
		assertThrows(IndexOutOfBoundsException.class, () -> font.glyph((char) 3));
		assertThrows(IndexOutOfBoundsException.class, () -> font.glyph((char) 5));
	}

	@DisplayName("The number of glyphs must fit within the texture tiles")
	@Test
	void tiles() {
		final var glyphs = Collections.nCopies(26, glyph);
		assertThrows(IllegalStateException.class, () -> new GlyphFont(4, glyphs, 5));
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
					.child("start", 4)
					.child("tiles", 5)
					.child()
						.name("glyphs")
						.child()
							.child("advance", 2)
							.child()
								.name("kerning")
								.child("W", 3)
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
