package org.sarge.jove.shader;

/**
 * Encapsulates the various shader parameter setters.
 * @author Sarge
 */
public interface ParameterMethod {
	/**
	 * Updates a shader parameter.
	 * @param param Parameter descriptor
	 */
	void update( ShaderParameter param );
}
