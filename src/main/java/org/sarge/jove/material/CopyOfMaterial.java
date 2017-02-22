package org.sarge.jove.material;

import java.util.Map;

import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.shader.ShaderProgram;

/**
 * Material descriptor.
 * @author Sarge
 */
public interface CopyOfMaterial {
	/**
	 * Shader parameter prefix for material properties.
	 */
	String MATERIAL_PREFIX = "m_";

	/**
	 * Shader parameter prefix for global properties.
	 */
	String GLOBAL_PREFIX = "g_";

	// TODO - the g_ and m_ prefix should only be applied when setting into the shader?
	// no point in storing them when we *know* what sort they are
	// plus repeated memory allocation for building strings, which could be minimised/re-used @ render time

	/**
	 * @return Material name
	 */
	String getName();

	/**
	 * @return Shader for this material or <tt>null</tt> to use ancestor
	 */
	ShaderProgram getShader();

	/**
	 * @return Render properties ordered by name
	 */
	Map<String, RenderProperty> getRenderProperties();

	/**
	 * Applies this material.
	 * @param ctx Rendering context
	 */
	void apply( RenderContext ctx );

	/**
	 * Resets this material.
	 */
	void reset( RenderContext ctx );
}
