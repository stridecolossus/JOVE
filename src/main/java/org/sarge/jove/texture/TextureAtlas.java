package org.sarge.jove.texture;

import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.lib.xml.Element.ElementException;
import org.sarge.lib.xml.ElementLoader;

/**
 * A <i>texture atlas</i> contains the coordinates meta-data for a packed texture.
 * @author Sarge
 * TODO - other texture attributes other than rectangle?
 */
public class TextureAtlas {
	private final Dimensions size;
	private final Map<String, Rectangle> atlas;

	/**
	 * Constructor.
	 * @param size		Atlas size
	 * @param atlas 	Texture coordinates ordered by name
	 */
	TextureAtlas(Dimensions size, Map<String, Rectangle> atlas) {
		this.size = notNull(size);
		this.atlas = Map.copyOf(atlas);
	}

	/**
	 * @return Size of this atlas
	 */
	public Dimensions size() {
		return size;
	}

	/**
	 * Looks up the coordinates of the given texture by name.
	 * @param name Texture name
	 * @return Texture coordinates
	 * @throws IllegalArgumentException if the texture cannot be found
	 */
	public Rectangle get(String name) {
		final Rectangle rect = atlas.get(name);
		if(rect == null) throw new IllegalArgumentException("Texture not found: " + name);
		return rect;
	}

	/**
	 * @return Texture coordinates ordered by texture name
	 */
	public Map<String, Rectangle> atlas() {
		return atlas;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Loader for an XML texture atlas.
	 */
	public static class Loader {
		private final ElementLoader loader = new ElementLoader();

		/**
		 * Loads an XML texture atlas.
		 * @param r XML reader
		 * @return Atlas
		 * @throws IOException if the file cannot be loaded
		 * @throws ElementException if the atlas cannot be parsed
		 */
		public TextureAtlas load(Reader r) throws IOException {
			final Element xml = loader.load(r);
			return load(xml);
		}

		/**
		 * Loads an XML texture atlas.
		 * @param xml XML
		 * @return Atlas
		 * @throws ElementException if the atlas cannot be parsed
		 */
		public TextureAtlas load(Element xml) {
			// Load atlas size
			final int width = xml.attribute("width").toInteger();
			final int height = xml.attribute("height").toInteger();

			// Load textures
			final Map<String, Rectangle> atlas = new HashMap<>();
			final Consumer<Element> mapper = e -> {
				final String name = e.attribute("n").toText();
				final int x = e.attribute("x").toInteger();
				final int y = e.attribute("y").toInteger();
				final int w = e.attribute("w").toInteger();
				final int h = e.attribute("h").toInteger();
				final Rectangle rect = new Rectangle(x, y, w, h);
				atlas.put(name, rect);
			};
			xml.children("sprite").forEach(mapper);

			// Create atlas
			return new TextureAtlas(new Dimensions(width, height), atlas);
		}
	}
}
