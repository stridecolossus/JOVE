package org.sarge.jove.obj;

import static org.sarge.lib.util.Check.notEmpty;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Colour;
import org.sarge.lib.collection.StrictMap;
import org.sarge.lib.collection.StrictSet;

/**
 * OBJ material descriptor.
 * @author Sarge
 */
public class ObjectMaterial {
	/**
	 * OBJ material library.
	 */
	public static class Library {
		private final Map<String, ObjectMaterial> lib = new StrictMap<>();

		/**
		 * Adds a material to this library.
		 * @param mat Material
		 */
		public void add(ObjectMaterial mat) {
			lib.put(mat.name(), mat);
		}

		/**
		 * Looks up a material.
		 * @param name Material name
		 * @return Material
		 * @throws IllegalArgumentException if the material cannot be found
		 */
		public ObjectMaterial find(String name) {
			final ObjectMaterial mat = lib.get(name);
			if(mat == null) throw new IllegalArgumentException("Unknown OBJ material: " + name);
			return mat;
		}
	}

	/**
	 * Texture map types.
	 * TODO - factor out as generic?
	 */
	public enum TextureMap {
		AMBIENT,
		DIFFUSE,
		SPECULAR,
		SPECULAR_HIGHLIGHT,
		ALPHA,
		BUMP,
	}

	private final String name;
	private final Set<Integer> illumination;
	private final Map<Colour.Type, Colour> cols;
	private final Map<TextureMap, String> textures;

	// TODO
	// - specular exponent
	// - displacement map, decal texture
	// - multiple illumination models? enum?
	// - this extends generic material?
	// - d/Tr transparency

	/**
	 * Constructor.
	 * @param name				Material name
	 * @param illumination		Illumination model(s)
	 * @param cols				Material colours
	 * @param textures			Texture map filenames
	 */
	public ObjectMaterial(String name, Set<Integer> illumination, Map<Colour.Type, Colour> cols, Map<TextureMap, String> textures) {
		this.name = notEmpty(name);
		this.illumination = Set.copyOf(illumination);
		this.cols = Map.copyOf(cols);
		this.textures = Map.copyOf(textures);
	}

	/**
	 * @return Material name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Illumination model(s)
	 */
	public Set<Integer> illumination() {
		return illumination;
	}

	/**
	 * @return Material colours
	 */
	public Map<Colour.Type, Colour> colours() {
		return cols;
	}

	/**
	 * @return Texture maps
	 */
	public Map<TextureMap, String> textures() {
		return textures;
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.reflectionEquals(this, this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Builder for an OBJ material.
	 */
	public static class Builder {
		private String name;
		private final Set<Integer> illumination = new StrictSet<>();
		private final Map<Colour.Type, Colour> cols = new StrictMap<>();
		private final Map<TextureMap, String> textures = new StrictMap<>();

		/**
		 * Constructor.
		 * @param name Material name
		 */
		public Builder(String name) {
			this.name = name;
		}

		/**
		 * Adds an illumination model.
		 * @param illumination Illumination model index
		 */
		public Builder illumination(int illumination) {
			this.illumination.add(illumination);
			return this;
		}

		/**
		 * Sets a material colour.
		 * @param type		Colour type
		 * @param col		Colour
		 */
		public Builder colour(Colour.Type type, Colour col) {
			cols.put(type, col);
			return this;
		}

		/**
		 * Sets a texture map filename.
		 * @param map			Texture map
		 * @param filename		Filename
		 */
		public Builder texture(TextureMap map, String filename) {
			textures.put(map, filename);
			return this;
		}

		/**
		 * Constructs this material.
		 * @return New material
		 */
		public ObjectMaterial build() {
			return new ObjectMaterial(name, illumination, cols, textures);
		}
	}
}
