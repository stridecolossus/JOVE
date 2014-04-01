package org.sarge.jove.texture;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.util.ImageLoader;
import org.sarge.jove.util.JoveImage;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Texture loader.
 * @author Sarge
 */
public class TextureLoader {
	private final ImageLoader loader;
	private final RenderingSystem sys;

	/**
	 * Constructor.
	 * @param loader	Image loader
	 * @param sys		Rendering system
	 */
	public TextureLoader( ImageLoader loader, RenderingSystem sys ) {
		Check.notNull( loader );
		Check.notNull( sys );

		this.loader = loader;
		this.sys = sys;
	}

	/**
	 * Loads a texture file.
	 * @param path Texture file-path
	 * @return Texture
	 * @throws IOException if the texture image cannot be loaded
	 */
	public Texture load( String path ) throws IOException {
		final JoveImage image = loader.load( path );
		return create( image );
	}

	/**
	 * Creates a texture from the given image.
	 * @param image Texture image
	 * @return Texture
	 */
	public Texture create( JoveImage image ) {
		// Define texture properties
		final TextureDescriptor info = new TextureDescriptor( image.getDimensions() );
		info.setTranslucent( image.hasAlpha() );

		// TODO - how to set other parameters?

		// Upload texture
		return sys.createTexture( new ByteBuffer[]{ image.getBuffer() }, info );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
