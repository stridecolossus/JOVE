package org.sarge.jove.shader;

/**
 * Shader parameter types.
 * @author Sarge
 */
public enum ParameterType {
	MATRIX( "FLOAT_MAT" ),
	FLOAT( "FLOAT" ),
	INTEGER( "INT" ),
	BOOLEAN( "BOOL" ),
	TEXTURE( "SAMPLER" );

	/**
	 * Parameter buffer types.
	 */
	static enum BufferType {
		FLOAT_BUFFER,
		INTEGER_BUFFER,
	}

	private final String type;

	private ParameterType( String type ) {
		this.type = type;
	}

	/**
	 * @return Buffer type for this parameter
	 */
	public BufferType getBufferType() {
		switch( this ) {
		case FLOAT:
		case MATRIX:
			return BufferType.FLOAT_BUFFER;

		default:
			return BufferType.INTEGER_BUFFER;
		}
	}

	/**
	 * Maps the given OpenGL parameter type name to the corresponding JOVE type.
	 * @param name Parameter type name
	 * @return JOVE parameter type or <tt>null</tt> if not found
	 */
	public static ParameterType map( String name ) {
		for( ParameterType type : ParameterType.values() ) {
			if( name.startsWith( type.type ) ) return type;
		}

		return null;
	}
}
