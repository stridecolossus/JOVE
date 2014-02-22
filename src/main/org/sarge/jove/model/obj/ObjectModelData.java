package org.sarge.jove.model.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.material.Material;
import org.sarge.jove.model.AbstractMesh;
import org.sarge.jove.model.MeshBuilder;
import org.sarge.jove.model.MeshLayout;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.scene.Node;
import org.sarge.jove.util.ImageLoader;
import org.sarge.lib.io.DataSource;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Mutable <tt>OBJ</tt> model data.
 * @author Sarge
 */
public class ObjectModelData {
	// Config
	private final DataSource src;
	private final RenderingSystem sys;

	// Material library
	private final Map<String, Material> library = new HashMap<>();

	// Current object data
	private final List<Point> vertices = new ArrayList<>();
	private final List<TextureCoord> coords = new ArrayList<>();
	private final List<Vector> normals = new ArrayList<>();

	// Current node
	private Node root = new Node( "object" );
	private Node node;
	private MeshBuilder builder;

	/**
	 * Constructor.
	 * @param src Data-source for materials and textures
	 * @param sys Rendering system
	 * @see #setMaterialLoaderDataSource(DataSource)
	 * @see #setImageLoader(ImageLoader)
	 */
	public ObjectModelData( DataSource src, RenderingSystem sys ) {
		Check.notNull( src );
		Check.notNull( sys );

		this.src = src;
		this.sys = sys;

		// Start anonymous node
		builder = new MeshBuilder( MeshLayout.create( Primitive.TRIANGLES, "VN0", false ) );
		//startNode( "model" );
	}

	/**
	 * @return Asset data-source
	 */
	public DataSource getDataSource() {
		return src;
	}

	/**
	 * @return Material library ordered by name
	 */
	public Map<String, Material> getMaterialLibrary() {
		return library;
	}

	/**
	 * Adds a new material to the library.
	 * @param mat Material
	 */
	public void add( Material mat ) {
		library.put( mat.getName(), mat );
	}

	/**
	 * @return Current node
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * Starts a new polygon group / object.
	 * @param name Object name
	 */
	public void startNode( String name ) {
		// Build pending node
		build();

		// Start new node
		node = new Node( name );
		builder.reset();
	}

	/**
	 * Constructs the pending node and mesh.
	 */
	private void build() {
		if( ( builder != null ) && !builder.getVertices().isEmpty() ) {
			builder.build();
			final AbstractMesh mesh = sys.createMesh( builder );
			node.setRenderable( mesh );
			root.add( node );
		}
	}

	/**
	 * Adds a new vertex.
	 * @param pos Vertex position
	 */
	public void add( Point pos ) {
		vertices.add( pos );
	}

	/**
	 * Looks up a vertex position by index.
	 * @param idx Index
	 * @return Vertex position
	 */
	public Point getVertex( int idx ) {
		return get( vertices, idx );
	}

	/**
	 * Adds a vertex normal.
	 * @param normal normal
	 */
	public void add( Vector normal ) {
		normals.add( normal );
	}

	/**
	 * Looks up a normal.
	 * @param idx Index
	 * @return Normal
	 */
	public Vector getNormal( int idx ) {
		return get( normals, idx );
	}

	/**
	 * Adds texture coordinates.
	 * @param tc Texture coordinates
	 */
	public void add( TextureCoord tc ) {
		coords.add( tc );
	}

	/**
	 * Looks up a texture coordinates.
	 * @param idx Index
	 * @return Texture coordinate
	 */
	public TextureCoord getTextureCoord( int idx ) {
		return get( coords, idx );
	}

	/**
	 * Looks up a item from the given list by index.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>index can be negative relative to the size of the list</li>
	 * <li>indices start at <b>one</b></li>
	 * </ul>
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	private static <T> T get( List<T> list, int idx ) {
		// Determine actual index
		final int actual;
		if( idx > 0 ) {
			// Convert to zero-based index
			actual = idx - 1;
		}
		else {
			// Index backwards
			actual = list.size() - Math.abs( idx );
		}

		// Verify index
		if( ( actual < 0 ) || ( actual >= list.size() ) ) throw new IndexOutOfBoundsException( "Invalid index: index=" + idx + " size=" + list.size() );

		// Lookup
		return list.get( actual );
	}

	/**
	 * Adds a vertex to the model.
	 * @param v Vertex
	 */
	public void add( Vertex v ) {
		builder.add( v );
	}

	/**
	 * @return Root node of this model
	 */
	public Node getRootNode() {
		build();
		return root;
	}

	@Override
	public String toString() {
		final ToString ts = new ToString( this );
		ts.append( "vertices", vertices.size() );
		ts.append( "coords", coords.size() );
		ts.append( "normals", normals.size() );
		ts.append( "materials", library.size() );
		ts.append( "nodes", root.getChildren().size() );
		return ts.toString();
	}
}
