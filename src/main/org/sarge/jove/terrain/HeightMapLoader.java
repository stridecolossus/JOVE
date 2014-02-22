package org.sarge.jove.terrain;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.sarge.jove.util.ImageLoader;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Loader for {@link HeightMap} data.
 * @author Sarge
 */
public class HeightMapLoader {
	private final ImageLoader loader;

	/**
	 * Constructor.
	 * @param loader Height-map image loader
	 */
	public HeightMapLoader( ImageLoader loader ) {
		Check.notNull( loader );
		this.loader = loader;
	}

	/**
	 * Loads a height-map from a gray-scale image.
	 * @param path Image file-path
	 * @return Height-map
	 * @throws IOException if the file cannot be loaded
	 */
	public HeightMap load( String path ) throws IOException {
		// Load the height-map image
		final BufferedImage image = loader.load( path );
		if( image == null ) throw new IOException( "Unsupported image format: " + path );

		// Generate height-map
		final HeightMap map = new MutableHeightMap( image );

		// Cleanup
		image.flush();

		return map;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
