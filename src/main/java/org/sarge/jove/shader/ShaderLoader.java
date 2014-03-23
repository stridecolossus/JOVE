package org.sarge.jove.shader;

import java.io.IOException;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.lib.io.DataSource;
import org.sarge.lib.io.TextLoader;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Loader for shader code.
 * @author Sarge
 */
public class ShaderLoader {
	private final TextLoader loader;
	private final RenderingSystem sys;

	/**
	 * Constructor.
	 * @param src Data-source
	 */
	public ShaderLoader( DataSource src, RenderingSystem sys ) {
		Check.notNull( sys );

		this.loader = new TextLoader( src );
		this.sys = sys;
	}

	/**
	 * Loads a shader.
	 * @param type		Shader type
	 * @param path		File-path
	 * @param flags		Set of conditional flags
	 * @return Shader
	 * @throws ShaderException
	 * @throws IOException
	 */
	public Shader load( Shader.Type type, String path, String flags ) throws ShaderException, IOException {
		String code = loader.load( path );
		if( flags != null ) code = flags + code;
		return sys.createShader( type, code );
	}

	/**
	 * Loads and compiles a shader program.
	 * @param paths		List of shaders
	 * @param flags		Set of conditional flags
	 * @return Shader program
	 * @throws ShaderException TODO
	 * @throws IOException if the shaders cannot be loaded
	 */
	public ShaderProgram load( String[] paths, String flags ) throws ShaderException, IOException {
		final Shader[] shaders = new Shader[ paths.length ];
		for( int n = 0; n < paths.length; ++n ) {
			// find filename extension
			final String path = paths[ n ];
			final int idx = path.lastIndexOf( '.' );
			if( idx == -1 ) throw new IllegalArgumentException( "Cannot find file extension: " + path );

			// Map to shader type
			final Shader.Type type = Shader.Type.getType( path.substring( idx + 1 ) );
			if( type == null ) throw new UnsupportedOperationException( "Cannot determine shader type: " + path );

			// Load shader
			shaders[ n ] = load( type, paths[ n ], flags );
		}

		// Create shader program
		return sys.createShaderProgram( shaders );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
