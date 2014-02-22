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
			final Shader.Type type = getType( paths[ n ] );
			shaders[ n ] = load( type, paths[ n ], flags );
		}

		// Create shader program
		return sys.createShaderProgram( shaders );
	}

	/**
	 * Determines the type of shader from the file extension.
	 * @param path Shader file-path
	 * @return Shader type
	 */
	public static Shader.Type getType( String path ) {
		if( path.endsWith( ".vert" ) ) {
			return Shader.Type.VERTEX;
		}
		if( path.endsWith( ".frag" ) ) {
			return Shader.Type.FRAGMENT;
		}
		if( path.endsWith( ".geom" ) ) {
			return Shader.Type.GEOMETRY;
		}
		else {
			throw new IllegalArgumentException( "Cannot determine shader type: " + path );
		}
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
