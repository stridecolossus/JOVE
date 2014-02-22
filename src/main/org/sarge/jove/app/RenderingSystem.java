package org.sarge.jove.app;

import java.awt.Canvas;
import java.nio.ByteBuffer;

import org.sarge.jove.common.Face;
import org.sarge.jove.material.BlendProperty;
import org.sarge.jove.material.DepthTestProperty;
import org.sarge.jove.material.PointSpriteProperty;
import org.sarge.jove.model.AbstractMesh;
import org.sarge.jove.model.MeshBuilder;
import org.sarge.jove.scene.Viewport;
import org.sarge.jove.shader.Shader;
import org.sarge.jove.shader.Shader.Type;
import org.sarge.jove.shader.ShaderException;
import org.sarge.jove.shader.ShaderProgram;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureDescriptor;

/**
 * Rendering system.
 * @author Sarge
 */
public interface RenderingSystem {
	/**
	 * Initialises the display.
	 * TODO - wont work for android
	 * @param canvas
	 */
	void init( Canvas canvas );

//	/**
//	 * @return Image loader for this rendering system
//	 */
//	ImageLoader getImageLoader();
//
// TODO
// - move all AWT/Swing stuff to LWJGL?


	/**
	 * Updates the display after rendering a frame.
	 */
	void update();

	/**
	 * Closes the display.
	 */
	void close();

	/**
	 * @return OpenGL version number
	 * TODO - this should be a much richer capabilities/version thing
	 */
	String getVersion();

	/**
	 * @return Creates a viewport
	 */
	Viewport createViewport();

	/**
	 * Sets the polygon face culling mode.
	 * @param face Face culling mode
	 */
	void setFaceCulling( Face face );

	/**
	 * Sets the winding order.
	 * @param order Winding order, <tt>true</tt> for default counter-clockwise order
	 */
	void setWindingOrder( boolean order );

	/**
	 * Sets polygon mode.
	 * @param wireframe Whether to render as wire-frames
	 */
	void setWireframeMode( boolean wireframe );

	/**
	 * Sets the depth test.
	 * @param test Depth test function or <tt>null</tt> to disable depth test
	 */
	void setDepthTest( DepthTestProperty test );

	/**
	 * Sets blending.
	 * @param blend Blending properties or <tt>null</tt> to disable
	 */
	void setBlend( BlendProperty blend );

	/**
	 * Sets point-sprite properties.
	 * @param props Point-sprite properties or <tt>null</tt> to disable
	 */
	void setPointSprites( PointSpriteProperty props );

	/**
	 * Creates a texture.
	 * @param buffers 	Texture data
	 * @param info		Descriptor
	 * @return New texture
	 */
	Texture createTexture( ByteBuffer[] buffers, TextureDescriptor info );

	/**
	 * Creates a VBO-based mesh renderer.
	 * @param builder Mesh builder
	 * @return Mesh
	 */
	AbstractMesh createMesh( MeshBuilder builder );

	/**
	 * Creates a shader.
	 * @param type Shader type
	 * @param code Shader GLSL code
	 * @return Shader
	 * @throws ShaderException if the shader cannot be created
	 */
	Shader createShader( Type type, String code ) throws ShaderException;

	/**
	 * Creates a shader program.
	 * @param shaders Shaders
	 * @return Shader program
	 * @throws ShaderException if the program cannot be created
	 */
	ShaderProgram createShaderProgram( Shader[] shaders ) throws ShaderException;
}
