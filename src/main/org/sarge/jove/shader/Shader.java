package org.sarge.jove.shader;

import org.sarge.jove.common.GraphicResource;

/**
 * Shader.
 * @author Sarge
 */
public interface Shader extends GraphicResource {
	/**
	 * Shader type.
	 */
	enum Type {
		VERTEX,
		FRAGMENT,
		GEOMETRY,
		TESSELATION_EVALUATION,
		TESSELATION,
	}
}
