package org.sarge.jove.model;

import static java.util.stream.Collectors.toMap;
import static org.sarge.lib.util.Check.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.io.ResourceLoader;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.Coordinate.Coordinate2D.Corners;
import org.sarge.lib.element.*;
import org.sarge.lib.util.Check;
import org.yaml.snakeyaml.Yaml;

/**
 * A <i>texture font</i> defines the properties of a {@link Glyph} based font.
 * <p>
 * Assumptions:
 * <ul>
 * <li>The associated texture image is square</li>
 * <li>Glyphs are arranged as a row-major grid</li>
 * <li>The range of characters is contiguous but can be offset by the {@link #start()} property</li>
 * </ul>
 * <p>
 * The {@link #metrics(String)} method builds the glyph {@link Metrics} for the characters of a given word, which can be used to layout text in this font.
 * <p>
 * A texture font can be persisted as YAML document using the {@link Loader}.
 * <p>
 * @author Sarge
 */
public class TextureFont {
	/**
	 * A <i>glyph</i> defines the layout properties of a character in this texture font.
	 */
	public record Glyph(char code, int advance, Map<Integer, Integer> kerning) {
		/**
		 * Empty kerning pairs.
		 */
		public static final Map<Integer, Integer> DEFAULT_KERNING = Map.of();

		/**
		 * Constructor.
		 * @param code			Code-point
		 * @param advance 		Character advance
		 */
		public Glyph {
			Check.zeroOrMore(code);
			Check.zeroOrMore(advance);
			kerning = Map.copyOf(kerning);
		}

		/**
		 * Constructor for a glyph without kerning metadata.
		 * @param advance Character advance
		 */
		public Glyph(char code, int advance) {
			this(code, advance, DEFAULT_KERNING);
		}

		/**
		 * Advance from this glyph to the next character taking into account kerning pairs.
		 * @param next Next character
		 * @return Character advance
		 */
		public int advance(int next) {
			return kerning.getOrDefault(next, advance);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("code", code)
					.append("advance", advance)
					.append("kerning", kerning.size())
					.build();
		}
	}

	private final char start;
	private final List<Glyph> glyphs;
	private final int size;
	private final int tiles;
	private final float width;
	private final int height;
	private final int leading;

	/**
	 * Constructor.
	 * @param start			Starting character
	 * @param glyphs		Glyphs
	 * @param size			Texture dimensions (pixels)
	 * @param tiles			Number of glyph tiles, i.e. the number of rows and columns
	 * @param height		Font height
	 * @param leading		Leading spacing between lines of this font
	 * @throws IllegalStateException if there are too many glyphs for the given number of texture {@link #tiles}
	 */
	public TextureFont(char start, List<Glyph> glyphs, int size, int tiles, int height, int leading) {
		this.start = start;
		this.glyphs = List.copyOf(glyphs);
		this.size = oneOrMore(size);
		this.tiles = oneOrMore(tiles);
		this.width = 1f / tiles;
		this.height = zeroOrMore(height);
		this.leading = zeroOrMore(leading);
		validate();
	}

	private void validate() {
		if(start < 0) {
			throw new IllegalArgumentException();
		}

		if(glyphs.size() > tiles * tiles) {
			throw new IllegalStateException("Number of glyphs exceeds the texture tiles");
		}
	}

	/**
	 * @return Starting character
	 */
	public char start() {
		return start;
	}

	/**
	 * @return Texture dimensions
	 */
	public int size() {
		return size;
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
	 * @return Font height
	 */
	public int height() {
		return height;
	}

	/**
	 * @return Leading spacing
	 */
	public int leading() {
		return leading;
	}

	/**
	 * Glyph <i>metrics</i> specify the layout properties of a glyph.
	 */
	public class Metrics {
		private final int index;
		private final int advance;

		/**
		 * Constructor.
		 * @param index			Glyph index
		 * @param advance		Character advance
		 */
		private Metrics(int index, int advance) {
			this.index = index;
			this.advance = advance;
		}

		/**
		 * @return Character advance (normalised)
		 */
		public float advance() {
			return advance / (float) size;
		}

		/**
		 * Helper - Calculates the total advance of the given word metrics.
		 * @param metrics Word metrics
		 * @return Total advance (normalised)
		 */
		public static float advance(List<Metrics> metrics) {
			return (float) metrics.stream().mapToDouble(Metrics::advance).sum();
		}

		/**
		 * Builds the texture coordinates for this glyph.
		 * @return Glyph coordinates
		 */
		public Corners coordinates() {
			final float x = (index % tiles) * width;
			final float y = (index / tiles) * width;
			final var topLeft = new Coordinate2D(x, y);
			final var bottomRight = new Coordinate2D(x + width, y + width);
			return new Corners(topLeft, bottomRight);
		}
	}

	/**
	 * Builds the glyph metrics for a given word.
	 * @param word Word
	 * @return Glyph metrics for each character of the word
	 */
	public List<Metrics> metrics(String word) {
		// Check for empty word
		if(word.isEmpty()) {
			return List.of();
		}

		// Build metrics for each pair of characters
		final int len = word.length();
		final var metrics = new ArrayList<Metrics>(len);
		for(int n = 1; n < len; ++n) {
			final char ch = word.charAt(n - 1);
			final char next = word.charAt(n);
			final Metrics m = metrics(ch, next);
			metrics.add(m);
		}

		// Build metrics for trailing character
		final char last = word.charAt(len - 1);
		final Metrics m = metrics(last, ' ');
		metrics.add(m);

		return metrics;
	}

	/**
	 * Builds the metrics for the given pair of characters.
	 * @param ch		Character
	 * @param next		Following character
	 * @return Glyph metrics
	 */
	private Metrics metrics(char ch, char next) {
		final int index = ch - start;
		final Glyph glyph = glyphs.get(index);
		final int advance = glyph.advance(next);
		return new Metrics(index, advance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, glyphs, size, tiles, height, leading);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof TextureFont that) &&
				(this.start == that.start()) &&
				(this.tiles == that.tiles()) &&
				(this.size == that.size()) &&
				(this.height == that.height()) &&
				(this.leading == that.leading()) &&
				this.glyphs.equals(that.glyphs);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("start", start)
				.append("glyphs", glyphs.size())
				.append("size", size)
				.append("tiles", tiles)
				.append("height", height)
				.append("leading", leading)
				.build();
	}

	/**
	 * Loader for a texture font.
	 * TODO - format
	 */
	public static class Loader implements ResourceLoader<Element, TextureFont> {
		private final YamlLoader loader = new YamlLoader();

		@Override
		public Element map(InputStream in) throws IOException {
			return loader.load(new InputStreamReader(in));
		}

		@Override
		public TextureFont load(Element doc) throws Exception {
			final char start = (char) doc.text("start").map(Integer::parseInt).orElse(0).intValue();
			final int size = parse(doc, "size");
			final int tiles = parse(doc, "tiles");
			final int height = parse(doc, "height");
			final int leading = parse(doc, "leading");
			final List<Glyph> glyphs = doc.child("glyphs").children().map(Loader::glyph).toList();
			return new TextureFont(start, glyphs, size, tiles, height, leading);
		}

		/**
		 * Loads glyph metrics.
		 */
		private static Glyph glyph(Element doc) {
			final char code = (char) parse(doc, "code");
			final int advance = parse(doc, "advance");
			final var kerning = doc.optional("kerning").map(Loader::kerning).orElse(Glyph.DEFAULT_KERNING);
			return new Glyph(code, advance, kerning);
		}

		/**
		 * Loads the kerning pairs for a glyph.
		 */
		private static Map<Integer, Integer> kerning(Element doc) {
			return doc
					.children()
					.map(Loader::pair)
					.collect(toMap(Entry::getKey, Entry::getValue));
		}

		/**
		 * Loads a kerning pair.
		 */
		private static Entry<Integer, Integer> pair(Element doc) {
			final int code = doc.name().transform(Integer::parseInt);
			final int advance = doc.text().transform(Integer::parseInt);
			return Map.entry(code, advance);
		}

		/**
		 * Helper - Parses a mandatory integer value.
		 */
		private static int parse(Element doc, String name) {
			return doc.child(name).text().transform(Integer::parseInt);
		}

		/**
		 * Outputs the given texture font as YAML document.
		 * @param font		Texture font to output
		 * @param out		Output
		 */
		public static void write(TextureFont font, Writer out) {
			// Write glyph metadata
			final var glyphs = font.glyphs
					.stream()
					.map(Loader::write)
					.toList();

			// Write font metadata
			final var data = Map.of(
					"start",		(int) font.start(),
					"size",		font.size(),
					"tiles",		font.tiles(),
					"height",		font.height(),
					"leading",		font.leading(),
					"glyphs", 		glyphs
			);

			// Output to a YAML document
			final Yaml yaml = new Yaml();
			yaml.dump(data, out);
		}

		/**
		 * Outputs a glyph.
		 */
		private static Object write(Glyph glyph) {
			// Output glyph metadata
			final var map = new HashMap<String, Object>();
			map.put("code", (int) glyph.code());
			map.put("advance", glyph.advance());

			// Output kerning pairs
			final var kerning = glyph.kerning();
			if(!kerning.isEmpty()) {
				map.put("kerning", kerning);
			}

			return map;
		}
	}
}
