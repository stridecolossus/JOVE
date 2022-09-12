package org.sarge.jove.util;

import static java.util.stream.Collectors.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.StreamSupport;

import org.json.*;
import org.sarge.jove.common.*;
import org.sarge.jove.io.ResourceLoader;

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
	// TODO - contiguous cube-map image?
	// TODO - factor out to separate helper?
	// TODO - do we need to expose the names?

	// TODO - grid atlas - Dimensions

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
	 * A texture atlas is specified as a JSON document illustrated by the following example:
	 * <p>
	 * <pre>
	 * {
	 *   atlas: [
	 *     {
	 *       name: rectangle,
	 *       rect: [1, 2, 3, 4]
	 *     }
	 *   ]
	 * }
	 * </pre>
	 */
	public static class Loader implements ResourceLoader<JSONObject, TextureAtlas> {
		@Override
		public JSONObject map(InputStream in) throws IOException {
			return new JSONObject(new JSONTokener(in));
		}

		@Override
		public TextureAtlas load(JSONObject root) throws IOException {
			// Extract array of entries
			final JSONArray array = root.getJSONArray("atlas");

			// Load texture atlas
			return StreamSupport
					.stream(array.spliterator(), false)
					.map(JSONObject.class::cast)
					.map(Loader::entry)
					.collect(collectingAndThen(toMap(Entry::getKey, Entry::getValue), TextureAtlas::new));
		}

		/**
		 * Loads a texture atlas entry.
		 */
		private static Entry<String, Rectangle> entry(JSONObject entry) {
			final String name = entry.getString("name").trim();
			final JSONArray array = entry.getJSONArray("rect");
			final Rectangle rect = new Rectangle(
					array.getInt(0),
					array.getInt(1),
					array.getInt(2),
					array.getInt(3)
			);
			return Map.entry(name, rect);
		}
	}
}
