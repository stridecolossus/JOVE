package org.sarge.jove.shader;

/**
 * Illegal GLSL code or invalid shader program.
 * @author Sarge
 */
public class ShaderException extends Exception {
	public ShaderException( String msg ) {
		super( msg );
	}
}
