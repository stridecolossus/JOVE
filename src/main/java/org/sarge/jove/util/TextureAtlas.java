package org.sarge.jove.util;

import static java.util.stream.Collectors.*;

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.StreamSupport;

import org.json.*;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.io.ResourceLoader;

/**
 * A <i>texture atlas</i> maps rectangles within a texture image by name.
 * @author Sarge
 */
public interface TextureAtlas<T> {
	/**
	 *
	 * @param key
	 * @return
	 */
	Rectangle quad(T key);

	/**
	 *
	 * @param <T>
	 * @param atlas
	 * @return
	 */
	static <T> TextureAtlas<T> of(Map<T, Rectangle> atlas) {
		return atlas::get;
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
	public static class Loader implements ResourceLoader<JSONObject, TextureAtlas<String>> {
		@Override
		public JSONObject map(InputStream in) throws IOException {
			return new JSONObject(new JSONTokener(in));
		}

		@Override
		public TextureAtlas<String> load(JSONObject root) throws IOException {
			// Extract array of entries
			final JSONArray array = root.getJSONArray("atlas");

			// Load texture atlas
			return StreamSupport
					.stream(array.spliterator(), false)
					.map(JSONObject.class::cast)
					.map(Loader::entry)
					.collect(collectingAndThen(toMap(Entry::getKey, Entry::getValue), TextureAtlas::of));
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
