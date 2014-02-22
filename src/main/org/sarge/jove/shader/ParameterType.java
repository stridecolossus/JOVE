package org.sarge.jove.shader;

import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.texture.TextureEntry;

/**
 * Shader parameter types.
 * @author Sarge
 */
public enum ParameterType {
	FLOAT( float.class ),
	INTEGER( int.class ),
	BOOLEAN( boolean.class ),
	MATRIX( Matrix.class ),
	TEXTURE( TextureEntry.class );

	private final Class<?> clazz;

	private ParameterType( Class<?> clazz ) {
		this.clazz = clazz;
	}

	/**
	 * @return Data-type of this parameter
	 */
	public Class<?> getParameterClass() {
		return clazz;
	}
}
