package org.sarge.jove.shader;

import java.util.Map;

import org.sarge.jove.common.AbstractGraphicResource;
import org.sarge.lib.util.StrictMap;
import org.sarge.lib.util.ToString;

/**
 * Template implementation.
 */
public abstract class AbstractShaderProgram extends AbstractGraphicResource implements ShaderProgram {
	protected final Map<String, ShaderParameter> params = new StrictMap<>();

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

        // Build parameters
        buildParameters( id );
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
	 * Builds the shader parameter map.
	 * @param program Program ID
	 */
	protected abstract void buildParameters( int program );

	/**
	 * Binds the given shader program.
	 * @param program Program ID
	 */
	protected abstract void bind( int program );

	@Override
	public ShaderParameter getParameter( String name ) {
		return params.get( name );
	}

	@Override
	public void update() {
		for( ShaderParameter p : params.values() ) {
			// Skip parameters that have not changed
			if( !p.isDirty() ) continue;

			// Check parameter value has been populated
			if( p.getValue() == null ) throw new IllegalArgumentException( "Parameter value not initialised: " + p );

			// Update
			update( p );
		}
	}

	/**
	 * Updates the given shader parameter.
	 * @param p Parameter
	 */
	protected abstract void update( ShaderParameter p );

	@Override
	public void activate() {
		bind( super.getResourceID() );
		update();
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
