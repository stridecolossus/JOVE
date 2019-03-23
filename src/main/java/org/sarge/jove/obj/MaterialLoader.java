package org.sarge.jove.obj;

import java.util.function.BiConsumer;

import org.sarge.jove.common.Colour;
import org.sarge.jove.obj.ObjectMaterial.TextureMap;

/**
 * Loader for an OBJ material.
 * @author Sarge
 */
public class MaterialLoader extends DefaultObjectLoader<ObjectMaterial.Builder> {
	/**
	 * Creates a colour parser.
	 * @param type Colour type
	 * @return Parser
	 */
	static Parser<ObjectMaterial.Builder> colour(Colour.Type type) {
		final BiConsumer<Colour, ObjectMaterial.Builder> setter = (col, model) -> model.colour(type, col);
		return new ArrayParser<>(3, Colour::new, setter);
	}

	/**
	 * Creates a texture-map parser.
	 * @param map Texture-map
	 * @return Parser
	 */
	static Parser<ObjectMaterial.Builder> texture(TextureMap map) {
		return (tokens, mat) -> mat.texture(map, tokens[1]);
	}

	/**
	 * Illumination model parser.
	 */
	static final Parser<ObjectMaterial.Builder> ILLUMINATION = (tokens, model) -> {
		final int illumination = Integer.parseInt(tokens[1]);
		model.illumination(illumination);
	};

	/**
	 * Constructor.
	 */
	public MaterialLoader() {
		// Register colour parsers
		add("Ka", colour(Colour.Type.AMBIENT));
		add("Kd", colour(Colour.Type.DIFFUSE));
		add("Ks", colour(Colour.Type.SPECULAR));

		// Register texture map parsers
		add("map_Ka", texture(TextureMap.AMBIENT));
		add("map_Kd", texture(TextureMap.DIFFUSE));
		add("map_Ks", texture(TextureMap.SPECULAR));
		add("map_Ns", texture(TextureMap.SPECULAR_HIGHLIGHT));
		add("map_d", texture(TextureMap.ALPHA));
		add("map_bump", texture(TextureMap.BUMP));

		// Register miscellaneous parsers
		add("illum", ILLUMINATION);
	}
}
