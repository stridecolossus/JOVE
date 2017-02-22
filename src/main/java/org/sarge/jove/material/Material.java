package org.sarge.jove.material;

import java.util.Optional;

import org.sarge.jove.shader.ShaderProgram;
import org.sarge.lib.util.Check;

/**
 * Material descriptor.
 * @author Sarge
 */
public final class Material {
	private final String name;
	private final Optional<ShaderProgram> shader;
	
	public Material(String name, Optional<ShaderProgram> shader) {
		Check.notEmpty(name);
		Check.notNull(shader);
		
		this.name = name;
		this.shader = shader;
	}

	public String getName() {
		return name;
	}
	
	public Optional<ShaderProgram> getShader() {
		return shader;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

//	/**
//	 * Shader parameter prefix for material properties.
//	 */
//	String MATERIAL_PREFIX = "m_";
//
//	/**
//	 * Shader parameter prefix for global properties.
//	 */
//	String GLOBAL_PREFIX = "g_";
//
//	// TODO - the g_ and m_ prefix should only be applied when setting into the shader?
//	// no point in storing them when we *know* what sort they are
//	// plus repeated memory allocation for building strings, which could be minimised/re-used @ render time
//
//	/**
//	 * @return Material name
//	 */
//	String getName();
//
//	/**
//	 * @return Shader for this material or <tt>null</tt> to use ancestor
//	 */
//	ShaderProgram getShader();
//
//	/**
//	 * @return Render properties ordered by name
//	 */
//	Map<String, RenderProperty> getRenderProperties();
//
//	/**
//	 * Applies this material.
//	 * @param ctx Rendering context
//	 */
//	void apply( RenderContext ctx );
//
//	/**
//	 * Resets this material.
//	 */
//	void reset( RenderContext ctx );
//}
