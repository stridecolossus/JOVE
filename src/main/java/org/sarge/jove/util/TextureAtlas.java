package org.sarge.jove.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.util.TextLoader.TextResourceLoader;

/**
 * A <i>texture atlas</i> maps rectangles within a texture image by name.
 * <p>
 * Note that atlas entries are <b>ordered</b>.
 * <p>
 * @author Sarge
 */
public class TextureAtlas extends LinkedHashMap<String, Rectangle> {
	/**
	 * Creates a texture atlas for a cube-map with the given image dimensions.
	 * <p>
	 * The atlas entries are ordered with the following key names: <code>+X -X +Y -Y +Z -Z</code>.
	 * <p>
	 * The cube-map image is assumed to have the following layout:
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
	 */
	public static class Loader extends TextResourceLoader<Entry<String, Rectangle>, TextureAtlas> {
		@Override
		protected Entry<String, Rectangle> load(String line) {
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

		@Override
		protected Collector<Entry<String, Rectangle>, ?, TextureAtlas> collector() {
			final var map = Collectors.toMap(Entry<String, Rectangle>::getKey, Entry::getValue);
			return Collectors.collectingAndThen(map, TextureAtlas::new);
		}
	}
}
