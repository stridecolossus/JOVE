package org.sarge.jove.model.obj;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.material.BlendProperty;
import org.sarge.jove.material.Material;
import org.sarge.jove.material.MaterialProperty;
import org.sarge.jove.material.MutableMaterial;
import org.sarge.jove.shader.ShaderLoader;
import org.sarge.jove.shader.ShaderProgram;
import org.sarge.jove.texture.TextureLoader;
import org.sarge.lib.io.DataSource;
import org.sarge.lib.io.FileDataSource;
import org.sarge.lib.io.TextLoader;
import org.sarge.lib.io.TextLoader.LineParser;
import org.sarge.lib.util.StrictMap;

/**
 * Loader for <tt>OBJ</tt> materials.
 * @author Sarge
 */
public class ObjectMaterialLoader {
	private static final Logger LOG = Logger.getLogger( ObjectMaterialLoader.class.getName() );

	private static final String NEW_MATERIAL = "newmtl";

	private final Map<String, ObjectMaterialLineParser> registry = new StrictMap<>();

	/**
	 * Registers a material command parser.
	 * @param cmd		Command identifier
	 * @param p			Parser
	 */
	public void register( String cmd, ObjectMaterialLineParser p ) {
		if( cmd.equals( NEW_MATERIAL ) ) throw new IllegalArgumentException( "Cannot over-ride new material parser" );
		registry.put( cmd, p );
	}

	/**
	 * Material file line parser.
	 */
	private LineParser parser = new LineParser() {
		@Override
		public void parse( String line, int lineno ) {
			// Tokenize line into command and args
			final String cmd;
			final String[] args;
			final int idx = line.indexOf( ' ' );
			if( idx == -1 ) {
				cmd = line;
				args = null;
			}
			else {
				cmd = line.substring( 0, idx );
				args = line.substring( idx + 1 ).split( " " );
			}

			if( cmd.equals( NEW_MATERIAL ) ) {
				// Start new material
				final String name = ObjectModelHelper.toString( args, "Expected material name" );
				material = createMaterial( name );
				lib.put( name, material );
			}
			else {
				// Lookup parser
				final ObjectMaterialLineParser p = registry.get( cmd );
				if( p == null ) {
					LOG.log( Level.WARNING, "Unsupported material command: " + cmd + " at line " + lineno );
					return;
				}

				// Delegate
				p.parse( args, material );
			}
		}
	};

	private final TextLoader loader;
	private final RenderingSystem sys;

	private MutableMaterial material;
	private Map<String, Material> lib;

	/**
	 * Constructor.
	 * @param src Material data-source
	 * @param loader	Loader for
	 * @param sys
	 */
	public ObjectMaterialLoader( DataSource src, RenderingSystem sys ) {
		this.sys = sys;

		// Init
		loader = new TextLoader( src );
		loader.setCommentIdentifier( "#" );

		// Register default parsers
		register( src );
	}

	private void register( DataSource src ) {
		// Colours
		register( "Ka", new ColourObjectMaterialLineParser( "Ka" ) );
		register( "Kd", new ColourObjectMaterialLineParser( "Kd" ) );
		register( "Ks", new ColourObjectMaterialLineParser( "Ks" ) );

		// Illumination model
		register( "illum", new IlluminationModelObjectMaterialLineParser() );

		// Textures
		final TextureLoader textureLoader = new TextureLoader( sys.getImageLoader( src ), sys );
		register( "map_Kd", new TextureObjectMaterialLineParser( "colourMap", textureLoader ) );

		// TODO
		// - Ns			specular coefficient 0..1000
		// - Tr or d	transparency
		// - map_Kd		diffuse (usually same as ambient)
		// - map_Ks		specular
		// - map_Ns		highlight component
		// - map_d		alpha texture
		// - map_bump	bump texture (can be bump)
		// - options
		// http://en.wikipedia.org/wiki/Wavefront_.obj_file
	}

	/**
	 * Loads an OBJ material library.
	 * @param path		Material file-path
	 * @param library	Material library
	 * @throws RuntimeException if the file cannot be parsed
	 */
	public void load( String path, Map<String, Material> library ) {
		try {
			this.lib = library;
			loader.load( parser, path );
		}
		catch( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	// TODO - this is too inflexible, e.g. what if we dont need normal-matrix, or model is blended?
	protected MutableMaterial createMaterial( String name ) {
		final MutableMaterial mat = new MutableMaterial( name );

		try {
			final ShaderLoader shaderLoader = new ShaderLoader( new FileDataSource( new File( "resource/shader" ) ), sys );
			final ShaderProgram shader = shaderLoader.load( new String[]{ "default.vert", "default.frag" }, null );
			mat.setShader( shader );
		}
		catch( Exception e ) {
			throw new RuntimeException( e );
		}

		mat.set( "pvmMatrix", MaterialProperty.PROJECTION_MODELVIEW_MATRIX );
		mat.set( "modelviewMatrix", MaterialProperty.MODELVIEW_MATRIX );
		mat.set( "normalMatrix", MaterialProperty.NORMAL_MATRIX );

		mat.add( new BlendProperty( "sa", "1-sa" ) );

		return mat;
	}
}
