package org.sarge.jove.util;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.sarge.lib.io.DataSource;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Default image loader.
 * TODO - should this be an interface?
 * @author Sarge
 */
public class ImageLoader {
	private final DataSource src;

	/**
	 * Constructor.
	 * @param src Data-source
	 */
	public ImageLoader( DataSource src ) {
		Check.notNull( src );
		this.src = src;
	}

	/**
	 * Loads an image.
	 * @param path Path
	 * @return Image
	 * @throws IOException if the file is not found or the image format is unknown
	 */
	public BufferedImage load( String path ) throws IOException {
		// TODO - this will only work for desktop? or is ImageIO available on android as well?
		return ImageIO.read( src.open( path ) );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
