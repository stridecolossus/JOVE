package org.sarge.jove.shader;

import java.util.Map;

import org.sarge.jove.common.AbstractGraphicResource;
import org.sarge.lib.util.StrictMap;
import org.sarge.lib.util.ToString;

/**
 * Template implementation.
 */
public abstract class AbstractShaderProgram extends AbstractGraphicResource implements ShaderProgram {
	private final Map<String, ShaderParameter> params = new StrictMap<>();

	/**
	 * Constructor.
	 * @param shaders Shaders
	 */
	protected AbstractShaderProgram( Shader[] shaders ) {
        // Allocate program ID
		// TODO - can this be moved into common ctor (in AbstractGraphicResource) for *all* graphic resources?
        final int id = allocate();
        setResourceID( id );

        // Attach shaders
        for( Shader shader : shaders ) {
        	attach( id, shader.getResourceID() );
        }

        // Link program
        link( id );

        // Build shader parameters
        final int num = getParameterCount();
        for( int n = 0; n < num; ++n ) {
        	// Get descriptor
        	final ParameterDescriptor desc = getShaderParameterDescriptor( n );

        	// Skip built-in parameters
        	final String name = desc.getName();
        	if( name.startsWith( "gl_" ) ) continue;

        	// Map parameter type
        	final ParameterType type = ParameterType.map( desc.getType().substring( 3 ) );
        	if( type == null ) throw new UnsupportedOperationException( "Unknown OpenGL type: " + desc.getType() );

        	// Determine component size
        	final int size = mapComponentSize( desc.getType() );

        	// Create parameter
        	final ShaderParameter p = new ShaderParameter( name, type, size, desc.getLength(), desc.getLocation() );
        	params.put( name, p );
        }
	}

	/**
	 * Maps the given OpenGL parameter type to its corresponding component size (using the last digit in the name if present).
	 * @param type Parameter type name
	 * @return Component size
	 */
	private static int mapComponentSize( String type ) {
		final char ch = type.charAt( type.length() - 1 );
		if( Character.isDigit( ch ) ) {
			return Character.getNumericValue( ch );
		}
		else {
			return 1;
		}
	}

	/**
	 * Allocates a shader program.
	 * @return Program ID
	 */
	protected abstract int allocate();

	/**
	 * Attaches a shader.
	 * @param program	Shader program ID
	 * @param shader	Shader ID
	 */
	protected abstract void attach( int program, int shader );

	/**
	 * Links this program.
	 * @param program Program ID
	 */
	protected abstract void link( int program );

	/**
	 * @return Number of shader parameters
	 */
	protected abstract int getParameterCount();

	/**
	 * Retrieves a shader parameter descriptor.
	 * @param idx Parameter index
	 * @return Descriptor
	 */
	protected abstract ParameterDescriptor getShaderParameterDescriptor( int idx );

	/**
	 * Binds the given shader program.
	 * @param program Program ID
	 */
	protected abstract void bind( int program );

	@Override
	public ShaderParameter getParameter( String name ) {
		return params.get( name );
	}

	/**
	 * @return Whether all parameters for this program have been initialised.
	 */
	public boolean isInitialised() {
		for( ShaderParameter p : params.values() ) {
			if( !p.isDirty() ) return false;
		}

		return true;
	}

	@Override
	public void activate() {
		bind( super.getResourceID() );
	}

	@Override
	public void reset() {
		bind( 0 );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
