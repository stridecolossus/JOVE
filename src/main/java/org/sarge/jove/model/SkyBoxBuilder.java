package org.sarge.jove.model;

import java.nio.ByteBuffer;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.FaceCulling;
import org.sarge.jove.material.DepthTestProperty;
import org.sarge.jove.material.FaceCullProperty;
import org.sarge.jove.material.MaterialProperty;
import org.sarge.jove.material.MutableMaterial;
import org.sarge.jove.scene.NodeGroup;
import org.sarge.jove.scene.RenderQueue;
import org.sarge.jove.scene.RenderableNode;
import org.sarge.jove.scene.SceneNode;
import org.sarge.jove.shader.ShaderLoader;
import org.sarge.jove.shader.ShaderProgram;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureDescriptor;
import org.sarge.jove.texture.TextureDescriptor.WrapPolicy;
import org.sarge.jove.util.ImageLoader;
import org.sarge.jove.util.JoveImage;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Sky-box builder.
 * @author Sarge
 */
public class SkyBoxBuilder {
	private final RenderingSystem sys;
	private final ImageLoader imageLoader;
	private final ShaderLoader shaderLoader;

	/**
	 * Constructor.
	 * @param sys			Rendering system
	 * @param imageLoader	Image loader for texture cube
	 * @param shaderLoader	Shader loader
	 */
	public SkyBoxBuilder( RenderingSystem sys, ImageLoader imageLoader, ShaderLoader shaderLoader ) {
		Check.notNull( sys );
		Check.notNull( imageLoader );
		Check.notNull( shaderLoader );

		this.sys = sys;
		this.imageLoader = imageLoader;
		this.shaderLoader = shaderLoader;
	}

	/**
	 * Builds a sky-box.
	 * @param size		Box size
	 * @param paths		Texture file-paths (5 or 6)
	 * @return Sky-box node
	 * @throws Exception if the textures or shader cannot be loaded
	 */
	public NodeGroup build( float size, String[] paths ) throws Exception {
		Check.notEmpty( paths );
		if( ( paths.length < 5 ) || ( paths.length > 6 ) ) throw new IllegalArgumentException( "Invalid number of textures" );

		// Create node for sky-box
		final SceneNode root = new SceneNode( "skybox", RenderQueue.Default.NONE );

		// Construct sky-box
		final CubeBuilder cubeBuilder = new CubeBuilder( MeshLayout.create( Primitive.TRIANGLES, "V", false ) );
		final MeshBuilder meshBuilder = cubeBuilder.create( size );
		final AbstractMesh mesh = sys.createMesh( meshBuilder.build() );
		final RenderableNode sky = new RenderableNode( "sky", RenderQueue.Default.SKY, mesh );
		sky.setParent( root );

		// Create material
		final MutableMaterial mat = new MutableMaterial( "skybox" );
		root.setMaterial( mat );

		// Load texture images
		Dimensions dim = new Dimensions( 0, 0 );
		final ByteBuffer[] buffers = new ByteBuffer[ paths.length ];
		for( int n = 0; n < buffers.length; ++n ) {
			// Load image
			final JoveImage image = imageLoader.load( paths[ n ] );
			buffers[ n ] = image.getBuffer();

			// Verify size
			if( n == 0 ) {
				dim = image.getDimensions();
			}
			else {
				if( !dim.equals( image.getDimensions() ) ) {
					throw new IllegalArgumentException( "Mis-matched image size: index=" + n );
				}
			}
		}

		// Define sky-box texture properties
		final TextureDescriptor info = new TextureDescriptor( dim, true );
		info.setMipMapped( false );
		info.setWrapPolicy( WrapPolicy.CLAMP_TO_EDGE );
		info.setTranslucent( false );

		// Load shader
		final ShaderProgram shader = shaderLoader.load( new String[]{ "shader/skybox.vert", "shader/skybox.frag" }, null );
		mat.setShader( shader );
		mat.set( "projection", MaterialProperty.PROJECTION_MATRIX );
		mat.set( "view", MaterialProperty.VIEW_MATRIX );

		// Create cube-map texture
		final Texture cube = sys.createTexture( buffers, info );
		mat.set( "skybox", cube );

		// Cull front faces since the camera is inside the box
		mat.add( new FaceCullProperty( FaceCulling.FRONT ) );

		// Ensure sky-box only drawn on empty buffer
		mat.add( new DepthTestProperty( "<=" ) );

		return root;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
