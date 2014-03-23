package org.sarge.jove.shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.sarge.jove.common.GraphicResource;

/**
 * Shader program.
 * @author Sarge
 */
public interface ShaderProgram extends GraphicResource {
	/**
	 * Activates this shader program.
	 */
	void activate();

	/**
	 * Deactivates this program.
	 */
	void reset();

	/**
	 * Retrieves a shader parameter by name.
	 * @param name Parameter name
	 * @return Parameter descriptor
	 */
	ShaderParameter getParameter( String name );

	/**
	 * Sets an integer or texture parameter.
	 * @param loc Parameter location
	 * @param buffer Buffered integer(s)
	 */
	void setInteger( int loc, IntBuffer buffer );

	/**
	 * Sets a floating-point parameter.
	 * @param loc		Parameter location
	 * @param size		Component size
	 * @param buffer Buffered float(s)
	 */
	void setFloat( int loc, int size, FloatBuffer buffer );

	/**
	 * Sets a matrix parameter.
	 * @param loc		Parameter location
	 * @param size		Component size
	 * @param buffer Buffered matrix(s)
	 */
	void setMatrix( int loc, int size, FloatBuffer buffer );
}
