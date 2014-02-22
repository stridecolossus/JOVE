package org.sarge.jove.model;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.Face;
import org.sarge.jove.material.DepthTestProperty;
import org.sarge.jove.material.FaceCullProperty;
import org.sarge.jove.material.MaterialProperty;
import org.sarge.jove.material.MutableMaterial;
import org.sarge.jove.scene.Node;
import org.sarge.jove.shader.ShaderLoader;
import org.sarge.jove.shader.ShaderProgram;
import org.sarge.jove.texture.MutableTextureDescriptor;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureDescriptor.WrapPolicy;
import org.sarge.jove.texture.TextureLoader;
import org.sarge.jove.util.ImageLoader;
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
	public Node build( float size, String[] paths ) throws Exception {
		Check.notEmpty( paths );
		if( ( paths.length < 5 ) || ( paths.length > 6 ) ) throw new IllegalArgumentException( "Invalid number of textures" );

		// Create node for sky-box
		final Node root = new Node( "skybox" );

		// Construct sky-box
		final CubeBuilder cubeBuilder = new CubeBuilder( MeshLayout.create( Primitive.TRIANGLES, "V", false ) );
		final MeshBuilder meshBuilder = cubeBuilder.create( size );
		meshBuilder.build();
		final AbstractMesh mesh = sys.createMesh( meshBuilder );
		root.setRenderable( mesh );

		// Create material
		final MutableMaterial mat = new MutableMaterial( "skybox" );
		root.setMaterial( mat );

		// Load texture images
		int w = 0, h = 0;
		final ByteBuffer[] buffers = new ByteBuffer[ paths.length ];
		for( int n = 0; n < buffers.length; ++n ) {
			final BufferedImage image = imageLoader.load( paths[ n ] );
			buffers[ n ] = TextureLoader.toBuffer( image, false );
			w = image.getWidth();
			h = image.getHeight();
		}

		// Define sky-box texture properties
		final MutableTextureDescriptor info = new MutableTextureDescriptor( w, h, true );
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
		mat.add( new FaceCullProperty( Face.FRONT ) );

		// Ensure sky-box only drawn on empty buffer
		mat.add( new DepthTestProperty( "<=" ) );

		return root;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
