package org.sarge.jove.model;

import static org.sarge.lib.util.Check.*;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.io.ResourceLoader;
import org.sarge.lib.element.*;
import org.yaml.snakeyaml.Yaml;

/**
 * A <i>glyph font</i> defines the properties of a texture based font where the {@link Glyph} for each character is arranged as a grid.
 * @author Sarge
 */
public class GlyphFont {
	private final int start;
	private final List<Glyph> glyphs;
	private final int tiles;

	// TODO - height, line thingy, etc

	/**
	 * Constructor.
	 * @param start		Starting character index
	 * @param glyphs	Glyphs
	 * @param tiles		Number of glyph tiles
	 * @throws IllegalStateException if there are too many glyphs for the given number of texture {@link #tiles}
	 */
	public GlyphFont(int start, List<Glyph> glyphs, int tiles) {
		this.start = zeroOrMore(start);
		this.glyphs = List.copyOf(glyphs);
		this.tiles = oneOrMore(tiles);
		validate();
	}

	private void validate() {
		if(glyphs.size() > tiles * tiles) {
			throw new IllegalStateException("Number of glyphs exceeds the texture tiles");
		}
	}

	/**
	 * @return Starting character
	 */
	public int start() {
		return start;
	}

	/**
	 * @return Number of glyphs
	 */
	public int glyphs() {
		return glyphs.size();
	}

	/**
	 * @return Number of glyph tiles
	 */
	public int tiles() {
		return tiles;
	}

	/**
	 * Retrieves the glyph for the given character.
	 * @param ch Character
	 * @return Glyph
	 * @throws IndexOutOfBoundsException is the character is out-of-bounds for this font
	 */
	public Glyph glyph(char ch) {
		return glyphs.get(ch - start);
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, glyphs, tiles);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof GlyphFont that) &&
				(this.start == that.start()) &&
				(this.tiles == that.tiles()) &&
				this.glyphs.equals(that.glyphs);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("start", start)
				.append("glyphs", glyphs.size())
				.append("tiles", tiles)
				.build();
	}

	/**
	 * Loader for a texture font.
	 * TODO - format
	 */
	public static class Loader implements ResourceLoader<Element, GlyphFont> {
		private final YamlLoader loader = new YamlLoader();

		@Override
		public Element map(InputStream in) throws IOException {
			return loader.load(new InputStreamReader(in));
		}

		@Override
		public GlyphFont load(Element data) throws Exception {
			final int start = data.text("start").map(Integer::parseInt).orElse(0);
			final int tiles = data.text("tiles").map(Integer::parseInt).orElse(16);
			final List<Glyph> glyphs = data.child("glyphs").children().map(Loader::glyph).toList();
			return new GlyphFont(start, glyphs, tiles);
		}

		/**
		 * Loads glyph metrics.
		 */
		private static Glyph glyph(Element data) {
			final float advance = data.child("advance").transform(Float::parseFloat);
			final var kerning = data.optional("kerning").map(Loader::kerning).orElse(Glyph.DEFAULT_KERNING);
			return new Glyph(advance, kerning);
		}

		/**
		 * Loads the kerning pairs for a glyph.
		 */
		private static Map<Character, Float> kerning(Element data) {
			// TODO
			return null;
		}

		/**
		 * Outputs the given texture font as YAML document.
		 * @param font		Texture font to output
		 * @param out		Output
		 */
		public static void write(GlyphFont font, Writer out) {
			// Write glyph metadata
			final var glyphs = font.glyphs
					.stream()
					.map(Glyph::write)
					.toList();

			// Write font metadata
			final var data = Map.of(
					"start",	font.start,
					"tiles",	font.tiles,
					"glyphs", 	glyphs
			);

			// Output to a YAML document
			final Yaml yaml = new Yaml();
			yaml.dump(data, out);
		}
	}
}
