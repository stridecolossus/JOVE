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
		glyph = new Glyph(2);
		font = new GlyphFont(3, List.of(glyph), 4);
	}

	@Test
	void constructor() {
		assertEquals(3, font.start());
		assertEquals(1, font.glyphs());
		assertEquals(4, font.tiles());
	}

	@DisplayName("A glyph can be retrieved by character")
	@Test
	void glyph() {
		assertEquals(glyph, font.glyph((char) 3));
	}

	@DisplayName("A glyph index must be within the available character range")
	@Test
	void bounds() {
		assertThrows(IndexOutOfBoundsException.class, () -> font.glyph((char) 2));
		assertThrows(IndexOutOfBoundsException.class, () -> font.glyph((char) 4));
	}

	@DisplayName("The number of glyphs must fit within the texture tiles")
	@Test
	void tiles() {
		final var glyphs = Collections.nCopies(17, glyph);
		assertThrows(IllegalStateException.class, () -> new GlyphFont(3, glyphs, 4));
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
					.child("start", 3)
					.child("tiles", 4)
					.child()
						.name("glyphs")
						.child()
							.child("advance", 5)
							.end()
						.child()
							.child("advance", 6)
							.child()
								.name("kerning")
								.child("W", 7)
								.end()
							.end()
						.end()
					.build();

			font = loader.load(root);
			assertEquals(2, font.glyphs());
			assertEquals(3, font.start());
			assertEquals(4, font.tiles());

			assertEquals(new Glyph(5), font.glyph((char) 3));
			assertEquals(new Glyph(6).kerning(Map.of('W', 7f)), font.glyph((char) 4));
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
