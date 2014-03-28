package org.sarge.jove.model.obj;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.model.ModelLoader;
import org.sarge.jove.scene.NodeGroup;
import org.sarge.lib.io.DataSource;
import org.sarge.lib.io.TextLoader;
import org.sarge.lib.io.TextLoader.LineParser;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictMap;

/**
 * Loader for <tt>OBJ</tt> format models.
 *
 * TODO
 * - this is a bit mixed up, separate into
 * - 1. load OBJ to model data
 * - 2. generate tasks for loading textures and building VBOs OR do it all in one go
 * - 3. build meshes and scene-graph
 * - 4. attach textures, materials, etc
 *
 *
 * @author Sarge
 */
public class ObjectModelLoader implements ModelLoader {
	private static final Logger LOG = Logger.getLogger( ObjectModelLoader.class.getName() );

	private final Map<String, ObjectLineParser> registry = new StrictMap<>();

	/**
	 * Registers an <tt>OBJ</tt> parser.
	 * @param cmd		Command identifier
	 * @param p			Parser
	 */
	public void register( String cmd, ObjectLineParser p ) {
		registry.put( cmd, p );
	}

	private final DataSource src;
	private final RenderingSystem sys;

	private final LineParser parser = new LineParser() {
		@Override
		public void parse( String line, int lineno ) {
			// Split line into command and arguments
			final String cmd;
			final String[] args;
			final int idx = findDelimiterIndex( line );
			if( idx == -1 ) {
				cmd = line;
				args = null;
			}
			else {
				cmd = line.substring( 0, idx );
				args = line.substring( idx + 1 ).split( " " );
				for( int n = 0; n < args.length; ++n ) args[ n ] = args[ n ].trim();
			}

			// Lookup parser
			final ObjectLineParser p = registry.get( cmd );
			if( p == null ) {
				LOG.log( Level.WARNING, "Unsupported command: " + cmd + " at line " + lineno );
				return;
			}

			// Delegate
			p.parse( args, data );
		}

		private int findDelimiterIndex( String line ) {
			int idx = 0;
			for( char ch : line.toCharArray() ) {
				if( Character.isWhitespace( ch ) ) return idx;
				++idx;
			}
			return -1;
		}
	};

	private final TextLoader loader;

	private ObjectModelData data;

	/**
	 * Constructor.
	 * @param src Data-source (for all assets)
	 * @param sys Rendering system
	 */
	public ObjectModelLoader( DataSource src, RenderingSystem sys ) {
		Check.notNull( src );
		Check.notNull( sys );

		this.src = src;
		this.sys = sys;

		loader = new TextLoader( src );
		loader.setSkipEmptyLines( true );
		loader.setCommentIdentifier( "#" );

		register();
	}

	private void register() {
		// Vertex data
		register( "v", new VertexObjectLineParser() );
		register( "vt", new TextureCoordObjectLineParser() );
		register( "vn", new NormalObjectLineParser() );

		// Polygons
		register( "f", new FaceObjectLineParser() );

		// Object groups
		register( "g", new GroupObjectLineParser() );
		register( "o", new GroupObjectLineParser() );

		// Material library
		register( "usemtl", new UseMaterialObjectLineParser() );
		register( "mtllib", new MaterialLibraryObjectLineParser( new ObjectMaterialLoader( src, sys ) ) );
	}

	@Override
	public NodeGroup load( String path ) throws IOException {
		// Create new model
		data = new ObjectModelData( src, sys );

		// Load model data
		loader.load( parser, path );

		// Build model
		final NodeGroup root = data.getRootNode();

		// Cleanup
		data = null;

		return root;
	}
}
