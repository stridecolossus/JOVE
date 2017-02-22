package org.sarge.jove.material;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.FaceCulling;
import org.sarge.jove.shader.Shader;
import org.sarge.jove.shader.ShaderLoader;
import org.sarge.jove.shader.ShaderProgram;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureUnit;
import org.sarge.jove.texture.TextureLoader;
import org.sarge.jove.util.ImageLoader;
import org.sarge.lib.io.DataSource;
import org.sarge.lib.io.DocumentElement;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;

/**
 * Loader for material definition files.
 * @author Sarge
 */
public class MaterialLoader {
	/**
	 * Convenience factory using a single data-source for material files, shaders and textures.
	 * @param src Data-source
	 * @param sys Rendering system
	 */
	public static MaterialLoader create( DataSource src, RenderingSystem sys ) {
		final ShaderLoader shaderLoader = new ShaderLoader( src, sys );
		final ImageLoader imageLoader = sys.getImageLoader( src );
		final TextureLoader textureLoader = new TextureLoader( imageLoader, sys );
		return new MaterialLoader( src, shaderLoader, textureLoader, sys );
	}

	private final DataSource src;
	private final ShaderLoader shaderLoader;
	private final TextureLoader textureLoader;
	private final RenderingSystem sys;

	/**
	 * Constructor.
	 * @param src				Data-source
	 * @param shaderLoader		Shader loader
	 * @param textureLoader		Texture image loader
	 * @param sys				Rendering system
	 */
	public MaterialLoader( DataSource src, ShaderLoader shaderLoader, TextureLoader textureLoader, RenderingSystem sys ) {
		Check.notNull( src );
		Check.notNull( shaderLoader );
		Check.notNull( textureLoader );
		Check.notNull( sys );

		this.src = src;
		this.shaderLoader = shaderLoader;
		this.textureLoader = textureLoader;
		this.sys = sys;
	}

	/**
	 * Loads a material from a definition file.
	 * @param path Definition file-path
	 * @return Material
	 * @throws Exception if the file descriptor is invalid or the material cannot be created
	 */
	public MaterialBuilder load( String path ) throws Exception {
		// Load XML
		final DocumentElement root = DocumentElement.load( src.open( path ) );

		// Create material
		final MaterialBuilder mat = new MaterialBuilder( root.getString( "name", null ) );
		final Set<String> flags = new HashSet<>();

		// Load colours
		for( DocumentElement e : root.getChildren( "colour" ) ) {
			final String name = e.getString( "name", null );
			final Colour col = loadColour( e );
			mat.set( name, col );
		}

		// Load floating-point values
		for( DocumentElement e : root.getChildren( "float" ) ) {
			final String name = e.getString( "name", null );
			final float f = Converter.FLOAT.convert( e.getText() );
			mat.set( name, f );
		}

		// Load textures
		int count = 0;
		for( DocumentElement e : root.getChildren( "texture" ) ) {
			// Allocate texture unit
			final String name = e.getString( "name", null );
			final int unit = e.getInteger( name, count++ );

			// Load texture image
			final String filepath = e.getText();
			final Texture tex = textureLoader.load( filepath );

			// TODO
			// - unit
			// - min/mag filters
			// - mipmap
			// - flip
			// - alpha?
			// - wrapping

			// Add to material
			final TextureUnit entry = new TextureUnit( tex, unit );
			mat.set( name, entry );

			// Add optional texture flag
			if( e.hasAttribute( "flag" ) ) {
				flags.add( e.getString( "flag", null ) );
			}
		}

		// Load built-in values
		for( DocumentElement e : root.getChildren( "property" ) ) {
			final String name = e.getString( "name", null );
			final MaterialProperty p = e.getEnum( "type", null, MaterialProperty.class );
			mat.set( name, p );
		}

		// Load render properties
		for( DocumentElement e : root.getChildren( "render" ) ) {
			final RenderProperty p = loadRenderProperty( e );
			mat.add( p );
		}

		// Load flags
		for( DocumentElement e : root.getChildren( "flag" ) ) {
			flags.add( e.getString( "name", null ) );
		}

		// Load shader program
		final ShaderProgram shader = loadShader( root, flags );
		mat.setShader( shader );

		return mat;
	}

	/**
	 * Loads the material shader program.
	 */
	private ShaderProgram loadShader( DocumentElement root, Set<String> flags ) throws Exception {
		// Create synthetic shader for flags
		final StringBuilder sb = new StringBuilder();
		for( String str : flags ) {
			sb.append( "#define " );
			sb.append( str );
			sb.append( '\n' );
		}
		final String code = sb.toString();

		// Load shaders
		final List<Shader> shaders = new ArrayList<>();
		for( DocumentElement e : root.getChildren( "shader" ) ) {
			final Shader.Type type = e.getEnum( "type", null, Shader.Type.class );
			final Shader shader = shaderLoader.load( type, e.getText(), code );
			shaders.add( shader );
		}

		// Stop if no shaders
		if( shaders.isEmpty() ) return null;

		// Link and validate shader program
		return sys.createShaderProgram( shaders.toArray( new Shader[]{} ) );
	}

	/**
	 * Loads a colour.
	 */
	private static Colour loadColour( DocumentElement root ) throws IOException {
		// Tokenize colour
		final String[] parts = root.getText().trim().split( "," );

		// Convert to floats
		final float[] col = new float[ parts.length ];
		for( int n = 0; n < parts.length; ++n ) {
			col[ n ] = Float.parseFloat( parts[ n ] );
		}

		// Convert to colour
		return new Colour( col );
	}

	/**
	 * Loads a render property.
	 */
	private static RenderProperty loadRenderProperty( DocumentElement root ) throws IOException {
		final String type = root.getString( "type", null );
		switch( type ) {
		case "blend":
			final String src = root.getString( "src", "1" );
			final String dest = root.getString( "dest", "1-sa" );
			return new BlendProperty( src, dest );

		case "depth-test":
			// TODO - is NEVER same as depthMask(false)??? do we need this 'disable' thing?
			final String test = root.getString( "test", null );
			if( test.equals( "disable" ) ) {
				return new DepthTestProperty( null );
			}
			else {
				return new DepthTestProperty( test );
			}

		case "point-sprite":
			return new PointSpriteProperty();

		case "face-cull":
			return new FaceCullProperty( root.getEnum( "face", null, FaceCulling.class ) );

		case "wireframe":
			return new WireframeProperty();

		default:
			throw new IOException( "Unsupported render property: " + type );
		}
	}
}
