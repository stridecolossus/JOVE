package org.sarge.jove.texture;

import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Hashtable;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.util.BufferUtils;
import org.sarge.jove.util.ImageLoader;
import org.sarge.lib.io.DataSource;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Texture loader.
 * TODO - should be interface/impl (ditto text, shader, image, etc)
 * @author Sarge
 */
public class TextureLoader {
	private static final ColorSpace SPACE = ColorSpace.getInstance( ColorSpace.CS_sRGB );
	private static final ColorModel TRANSLUCENT = createColourModel( true );
	private static final ColorModel OPAQUE = createColourModel( false );

	/**
	 * Creates OpenGL format colour models.
	 */
	private static ColorModel createColourModel( boolean hasAlpha ) {
		return new ComponentColorModel(
			SPACE,
			new int[]{ 8, 8, 8, hasAlpha ? 8 : 0 },
			hasAlpha,
			false,
			hasAlpha ? ComponentColorModel.TRANSLUCENT : ComponentColorModel.OPAQUE,
			DataBuffer.TYPE_BYTE
		);
	}

	/**
	 * Converts the texture image to OpenGL format prior to uploading to the hardware.
	 * @param src		Source image
	 * @param flip		Whether to flip the image vertically
	 * @return OpenGL texture image as a byte-buffer
	 */
	public static ByteBuffer toBuffer( BufferedImage src, boolean flip ) {
		// Select colour model
		final boolean hasAlpha = src.getColorModel().hasAlpha();
		final ColorModel model = hasAlpha ? TRANSLUCENT : OPAQUE;

		// Create raster
		final int w = src.getWidth();
		final int h = src.getHeight();
		final int bands = hasAlpha ? 4 : 3;
		final WritableRaster raster = Raster.createInterleavedRaster( DataBuffer.TYPE_BYTE, w, h, bands, null );

		// Create OpenGL image
		final BufferedImage dest = new BufferedImage( model, raster, false, new Hashtable<>() );

		// Draw OpenGL image
		final Graphics g = dest.getGraphics();
		if( flip ) {
			g.drawImage( src, 0, 0, w, h, 0, h, w, 0, null );
		}
		else {
			g.drawImage( src, 0, 0, null );
		}

		// Retrieve raw image data
        final DataBufferByte data = (DataBufferByte) dest.getRaster().getDataBuffer();

        // Convert to byte buffer
        final ByteBuffer buffer = BufferUtils.createByteBuffer( data.getData() );

        // Cleanup
        g.dispose();

        return buffer;
	}

	private final ImageLoader loader;
	private final RenderingSystem sys;

	private boolean flip = true;

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
	 * Constructor using default image loader.
	 * @param src Image data-source
	 * @param sys Rendering system
	 */
	public TextureLoader( DataSource src, RenderingSystem sys ) {
		this( new ImageLoader( src ), sys );
	}

	/**
	 * Sets whether to flip images vertically.
	 * @param flip Whether to flip images, default is <tt>true</tt>
	 */
	public void setFlip( boolean flip ) {
		this.flip = flip;
	}

	/**
	 * Loads a texture file.
	 * @param path Texture file-path
	 * @return Texture
	 * @throws IOException if the texture image cannot be loaded
	 */
	public Texture load( String path ) throws IOException {
		final BufferedImage image = loader.load( path );
		return create( image );
	}

	/**
	 * Creates a texture from the given image.
	 * @param image Texture image
	 * @return Texture
	 */
	public Texture create( BufferedImage image ) {
		// Convert image to buffer
		final ByteBuffer buffer = toBuffer( image, flip );

		// Define texture properties
		final MutableTextureDescriptor info = new MutableTextureDescriptor( image.getWidth(), image.getHeight() );
		info.setTranslucent( image.getColorModel().hasAlpha() );

		// TODO - how to set other parameters?

		// Upload texture
		return sys.createTexture( new ByteBuffer[]{ buffer }, info );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
