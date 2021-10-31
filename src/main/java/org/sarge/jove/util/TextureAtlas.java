package org.sarge.jove.util;

import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;

/**
 * A <i>texture atlas</i> maps rectangles within a texture image by name.
 * @author Sarge
 */
public class TextureAtlas extends LinkedHashMap<String, Rectangle> {
	/**
	 * Creates a texture atlas for a cube-map with the given image dimensions.
	 * <p>
	 * The atlas entries are ordered: <code>+X -X +Y -Y +Z -Z</code>.
	 * <p>
	 * The cube-map layout is assumed to be as follows:
	 * <p>
	 * <table border=1>
	 * <tr><td></td><td>top +Y 2</td><td></td><td></td></tr>
	 * <tr><td>back -X 1</td><td>left +Z 4</td><td>front +X 0</td><td>right -Z 5</td></tr>
	 * <tr><td></td><td>bottom -Y 3</td><td></td><td></td></tr>
	 * </table>
	 * <p>
	 * @param dim Image dimensions
	 * @return Cube-map atlas
	 */
	public static TextureAtlas cubemap(Dimensions dim) {
		final Map<String, Rectangle> cubemap = new LinkedHashMap<>();
		cubemap.put("+X", rectangle(dim, 2, 1));
		cubemap.put("-X", rectangle(dim, 0, 1));
		cubemap.put("+Y", rectangle(dim, 1, 0));
		cubemap.put("-Y", rectangle(dim, 1, 2));
		cubemap.put("+Z", rectangle(dim, 1, 1));
		cubemap.put("-Z", rectangle(dim, 3, 1));
		return new TextureAtlas(cubemap);
	}
	// TODO - offset within texture image? i.e. argument is rectangle = (offset, size)

	/**
	 * Helper - Builds a cube-map rectangle at the given X-Y location.
	 */
	private static Rectangle rectangle(Dimensions dim, int x, int y) {
		return new Rectangle(x * dim.width(), y * dim.height(), dim);
	}

	/**
	 * Constructor.
	 * @param atlas Texture atlas
	 */
	public TextureAtlas(Map<String, Rectangle> atlas) {
		super(atlas);
	}

	/**
	 * Loader for a texture atlas.
	 * <p>
	 * File format:
	 * <ul>
	 * <li>Each atlas entry has the following structure: <code>name x,y,w,h</code>.</li>
	 * <li>Empty lines are ignored</li>
	 * <li>Comments are indicated by the hash character</li>
	 * </ul>
	 */
	public static class TextureAtlasLoader implements ResourceLoader<Reader, TextureAtlas> {
		@Override
		public Reader map(InputStream in) throws IOException {
			return new InputStreamReader(in);
		}

		@Override
		public TextureAtlas load(Reader r) throws IOException {
			final Map<String, Rectangle> map = new BufferedReader(r)
					.lines()
					.map(String::trim)
					.filter(Predicate.not(String::isEmpty))
					.filter(line -> !line.startsWith("#"))
					.map(this::load)
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

			return new TextureAtlas(map);
		}

		/**
		 * Loads a texture atlas entry.
		 * @param line Line
		 * @return Atlas entry
		 */
		private Map.Entry<String, Rectangle> load(String line) {
			// Tokenize atlas entry
			final String[] parts = line.split(" ");
			if(parts.length != 2) throw new IllegalArgumentException("Invalid texture atlas entry");

			// Parse rectangle
			final int[] array = Arrays
					.stream(parts[1].split(","))
					.map(String::trim)
					.map(Integer::parseInt)
					.mapToInt(Integer::valueOf)
					.toArray();

			// Create rectangle
			if(array.length != 4) throw new IllegalArgumentException("Expected comma-delimited rectangle");
			final Rectangle rect = new Rectangle(array[0], array[1], array[2], array[3]);

			// Create atlas entry
			return Map.entry(parts[0].trim(), rect);
		}
	}
}
