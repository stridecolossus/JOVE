package org.sarge.jove.model;

import static org.sarge.lib.Validation.*;

import java.util.*;

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
		this.start = requireZeroOrMore(start);
		this.glyphs = List.copyOf(glyphs);
		this.tiles = requireOneOrMore(tiles);
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
}

//	/**
//	 * Loader for a texture font.
//	 * TODO - format
//	 */
//	public static class Loader implements ResourceLoader<Element, GlyphFont> {
//		private final YamlLoader loader = new YamlLoader();
//
//		@Override
//		public Element map(InputStream in) throws IOException {
//			return loader.load(new InputStreamReader(in));
//		}
//
//		@Override
//		public GlyphFont load(Element doc) throws Exception {
//			final int start = doc.text("start").map(Integer::parseInt).orElse(0);
//			final int tiles = doc.text("tiles").map(Integer::parseInt).orElse(16);
//			final List<Glyph> glyphs = doc.child("glyphs").children().map(Loader::glyph).toList();
//			return new GlyphFont(start, glyphs, tiles);
//		}
//
//		/**
//		 * Loads glyph metrics.
//		 */
//		private static Glyph glyph(Element doc) {
//			final int code = doc.child("code").transform(Integer::parseInt);
//			final float advance = doc.child("advance").transform(Float::parseFloat);
//			final var kerning = doc.optional("kerning").map(Loader::kerning).orElse(Glyph.DEFAULT_KERNING);
//			return new Glyph(code, advance, kerning);
//		}
//
//		/**
//		 * Loads the kerning pairs for a glyph.
//		 */
//		private static Map<Integer, Float> kerning(Element doc) {
//			return doc
//					.children()
//					.map(Loader::pair)
//					.collect(toMap(Entry::getKey, Entry::getValue));
//		}
//
//		private static Entry<Integer, Float> pair(Element doc) {
//			final int code = doc.name().transform(Integer::parseInt);
//			final float advance = doc.text().transform(Float::parseFloat);
//			return Map.entry(code, advance);
//		}
//
//		/**
//		 * Outputs the given texture font as YAML document.
//		 * @param font		Texture font to output
//		 * @param out		Output
//		 */
//		public static void write(GlyphFont font, Writer out) {
//			// Write glyph metadata
//			final var glyphs = font.glyphs
//					.stream()
//					.map(Loader::write)
//					.toList();
//
//			// Write font metadata
//			final var data = Map.of(
//					"start",	font.start,
//					"tiles",	font.tiles,
//					"glyphs", 	glyphs
//			);
//
//			// Output to a YAML document
//			final Yaml yaml = new Yaml();
//			yaml.dump(data, out);
//		}
//
//		/**
//		 * Outputs a glyph.
//		 */
//		private static Object write(Glyph glyph) {
//			// Output glyph metadata
//			final var map = new HashMap<String, Object>();
//			map.put("code", glyph.code());
//			map.put("advance", glyph.advance());
//
//			// Output kerning pairs
//			final var kerning = glyph.kerning();
//			if(!kerning.isEmpty()) {
//				map.put("kerning", kerning);
//			}
//
//			return map;
//		}
//	}
//}
