package org.sarge.jove.material;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.sarge.jove.shader.ShaderProgram;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureUnit;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictMap;

/**
 * Builder for a {@link Material}.
 * @author Sarge
 */
public class MaterialBuilder {
	private final String name;
	private final Map<String, MaterialProperty> parameters = new StrictMap<>();
//	private final Map<String, TextureUnit> textures = new LinkedHashMap<>();
//	private final Map<String, MaterialProperty> properties = new StrictMap<>();
	private final Map<String, RenderProperty> render = new StrictMap<>();

	private Optional<ShaderProgram> shader = Optional.empty();

	/**
	 * Constructor.
	 * @param name Material name
	 */
	public MaterialBuilder(String name) {
		Check.notEmpty(name);
		this.name = name;
	}

	/**
	 * Sets the shader program for this material.
	 * @param shader Shader
	 */
	public MaterialBuilder shader(ShaderProgram shader) {
		this.shader = Optional.of(shader);
		return this;
	}
	
	public MaterialBuilder add(String name, RenderProperty p) {
		render.put(name, p);
		return this;
	}

	/**
	 * Constructs a new material.
	 * @return New material
	 */
	public Material build() {
	}
}


//	/**
//	 * Sets a texture or colour-map.
//	 * @param name		Texture parameter name
//	 * @param unit		Texture unit
//	 * @throws IllegalArgumentException if the specified texture unit has already been used by this material
//	 */
//	public MaterialBuilder set(String name, TextureUnit unit) {
//		Check.notEmpty(name);
//		Check.notNull(unit);
//
//		for(TextureUnit entry : textures.values()) {
//			if(entry.getTextureUnit() == unit.getTextureUnit()) throw new IllegalArgumentException("Duplicate texture unit: " + name);
//		}
//
//		textures.put(MATERIAL_PREFIX + name, unit);
//	}
//
//	/**
//	 * Convenience method - Allocates the texture unit depending on insertion order.
//	 * @param name		Texture parameter name
//	 * @param tex		Texture
//	 */
//	public MaterialBuilder set(String name, Texture tex) {
//		final int idx = textures.size();
//		final TextureUnit unit = new TextureUnit(tex, idx);
//		set(name, unit);
//	}
//
//	/**
//	 * Sets a shader parameter.
//	 * @param name		Parameter name
//	 * @param value		Bufferable shader parameter value
//	 */
//	public MaterialBuilder set(String name, Object value) {
//		final String key = MATERIAL_PREFIX + name;
//		if(parameters.containsKey(key)) parameters.remove(key);
//		parameters.put(key, value);
//		modified.add(key);
//	}
//
//	/**
//	 * Adds a material property.
//	 * @param name		Property name
//	 * @param p			Material property
//	 */
//	public MaterialBuilder add(String name, MaterialProperty p) {
//		properties.put(GLOBAL_PREFIX + name, p);
//	}
//
//	/**
//	 * Adds a render property.
//	 * @param p Property
//	 * @throws IllegalArgumentException for a duplicate property
//	 */
//	public MaterialBuilder add(RenderProperty p) {
//		render.put(p.getType(), p);
//	}
}
