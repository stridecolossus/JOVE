package org.sarge.jove.shader;

import org.sarge.jove.common.GraphicResource;

/**
 * Shader program.
 * @author Sarge
 */
public interface ShaderProgram extends GraphicResource {
	/**
	 * Retrieves a shader parameter by name.
	 * @param name Parameter name
	 * @return Parameter descriptor
	 */
	ShaderParameter getParameter( String name );

	/**
	 * Updates all dirty shader parameters.
	 */
	void update();

	/**
	 * Activates this shader program.
	 */
	void activate();

	/**
	 * Deactivates this program.
	 */
	void reset();
}
