package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.Coordinate.Coordinate2D.Corners;
import org.sarge.jove.model.TextureFont.*;
import org.sarge.lib.element.Element;

class TextureFontTest {
	private TextureFont font;
	private Glyph a, b;

	@BeforeEach
	void before() {
		a = new Glyph('A', 2, Map.of((int) 'B', 3));
		b = new Glyph('B', 4);
		font = new TextureFont('A', List.of(a, b), 512, 16, 4, 5);
	}

	@Test
	void constructor() {
		assertEquals('A', font.start());
		assertEquals(2, font.glyphs());
		assertEquals(512, font.size());
		assertEquals(16, font.tiles());
		assertEquals(4, font.height());
		assertEquals(5, font.leading());
	}

	@DisplayName("The number of glyphs must fit within the texture tiles")
	@Test
	void tiles() {
		final var glyphs = Collections.nCopies(256 + 1, a);
		assertThrows(IllegalStateException.class, () -> new TextureFont('A', glyphs, 512, 16, 4, 5));
	}

	@DisplayName("The metrics for a word...")
	@Nested
	class MetricsTests {
		private Metrics metrics;

		@BeforeEach
		void before() {
			metrics = font.metrics("A").get(0);
		}

		@Test
		void advance() {
			assertEquals(2f / 512, metrics.advance());
		}

		@DisplayName("provide the texture coordinate of the character glyph")
		@Test
		void coordinates() {
			final float w = 1f / 16;
			final var topLeft = new Coordinate2D(0, 0);
			final var bottomRight = new Coordinate2D(w, w);
			assertEquals(new Corners(topLeft, bottomRight), metrics.coordinates());
		}

		@DisplayName("can be used to calculate the total advance of that word")
		@Test
		void total() {
			final var word = font.metrics("AAA");
			assertEquals(3, word.size());
			assertEquals((2 * 3) / 512f, Metrics.advance(word));
		}

		@DisplayName("include kerning pairs when calculating the total advance of the word")
		@Test
		void kerning() {
			final var word = font.metrics("AB");
			assertEquals((3 + 4) / 512f, Metrics.advance(word));
		}
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
					.child("size", 512)
					.child("tiles", 16)
					.child("height", 4)
					.child("leading", 5)
					.child()
						.name("glyphs")
						.child()
							.child("code", (int) 'A')
							.child("advance", 2)
							.child()
								.name("kerning")
								.child(String.valueOf((int) 'B'), 3)
							.end()
						.end()
						.child()
    						.child("code", (int) 'B')
    						.child("advance", 4)
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

			// Load back in and compare
			final var in = new ByteArrayInputStream(out.toString().getBytes());
			final Element data = loader.map(in);
			assertEquals(font, loader.load(data));
		}
	}
}
