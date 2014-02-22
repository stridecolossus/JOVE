package org.sarge.jove.model.md5;

import java.io.IOException;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.material.ExtendedMaterial;
import org.sarge.jove.material.MaterialProperty;
import org.sarge.jove.material.MutableMaterial;
import org.sarge.jove.model.AbstractMesh;
import org.sarge.jove.model.MeshBuilder;
import org.sarge.jove.model.MeshLayout;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.model.md5.MD5Model.Joint;
import org.sarge.jove.model.md5.MD5Model.MeshData;
import org.sarge.jove.model.md5.MD5Model.Triangle;
import org.sarge.jove.model.md5.MD5Model.VertexData;
import org.sarge.jove.model.md5.MD5Model.Weight;
import org.sarge.jove.scene.Node;
import org.sarge.jove.shader.ShaderException;
import org.sarge.jove.shader.ShaderLoader;
import org.sarge.jove.shader.ShaderProgram;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureLoader;
import org.sarge.lib.util.Check;

/**
 * MD5 model builder.
 * @author Sarge
 */
public class MD5ModelBuilder {
	private final TextureLoader textureLoader;
	private final ShaderLoader shaderLoader;

	/**
	 * Constructor.
	 * @param textureLoader		Loader for model textures
	 * @param shaderLoader		Shader loader
	 */
	public MD5ModelBuilder( TextureLoader textureLoader, ShaderLoader shaderLoader ) {
		Check.notNull( textureLoader );
		Check.notNull( shaderLoader );

		this.textureLoader = textureLoader;
		this.shaderLoader = shaderLoader;
	}

	/**
	 * Builds a scene-graph from the given MD5 model.
	 * @param model		MD5 model
	 * @param sys		Rendering system
	 * @return Model
	 * @throws IOException if a texture cannot be loaded
	 * @throws ShaderException if a shader cannot be loaded or compiled
	 */
	public Node buildModel( MD5Model model, RenderingSystem sys ) throws IOException, ShaderException {
		// Create model root node
		final Node root = new Node( "MD5" );

		// Create shared material
		final ShaderProgram shader = shaderLoader.load( new String[]{ "shader/texture.vert", "shader/texture.frag" }, null );
		final MutableMaterial rootMaterial = new MutableMaterial( "MD5" );
		rootMaterial.setShader( shader );
		rootMaterial.set( "m_pvm", MaterialProperty.PROJECTION_MODELVIEW_MATRIX );
		rootMaterial.set( "m_vm", MaterialProperty.MODELVIEW_MATRIX );

		// Build model scene-graph
		for( MeshData mesh : model.meshes ) {
			// Build mesh vertices
			buildVertices( mesh, model.joints );

			// Create node for each mesh
			final Node node = new Node( mesh.texture );
			root.add( node );

			// Construct model from raw data
			final MeshBuilder builder = new MeshBuilder( MeshLayout.create( Primitive.TRIANGLES, "VN0", false ) );
			for( Triangle tri : mesh.triangles ) {
				for( int n = 0; n < 3; ++n ) {
					// Lookup vertex for each triangle corner
					final int idx = tri.index[ n ];
					final VertexData data = mesh.vertices[ idx ];

					// Create mesh vertex
					final Vertex v = new Vertex( data.pos );
					v.setTextureCoords( data.coords );
					builder.add( v );
				}
			}
			builder.computeNormals();
			builder.build();

			// Construct mesh
			final AbstractMesh m = sys.createMesh( builder );
			node.setRenderable( m );

			// Attach material
			final MutableMaterial mat = new ExtendedMaterial( mesh.texture, rootMaterial );
			node.setMaterial( mat );

			// Load texture
			final Texture tex = textureLoader.load( mesh.texture );
			mat.set( "m_tex", tex );
		}

		return root;
	}

	/**
	 * Generates mesh vertex positions from joint-weights.
	 * TODO - drop weights once this is done?
	 */
	private static void buildVertices( MeshData mesh, Joint[] joints ) {
		// Build weighted vertex positions
		for( VertexData vertex : mesh.vertices ) {
			// Init vertex position
			vertex.pos = new Point();

			// Calc final position as sum of all weight positions
			for( int c = 0; c < vertex.count; ++c ) {
				// Lookup weight
				final int idx = vertex.start + c;
				final Weight w = mesh.weights[ idx ];

				// Lookup joint this weight is attached to
				final Joint joint = joints[ w.jointIndex ];

				// Convert weight position from joint-local to object space
				final Point pos = joint.rot.rotate( w.pos ).add( joint.pos );

				// Apply bias weighting
				vertex.pos = vertex.pos.add( pos.multiply( w.bias ) );
			}
		}
	}
}
