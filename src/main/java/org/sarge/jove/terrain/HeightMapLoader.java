package org.sarge.jove.terrain;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.util.ImageLoader;
import org.sarge.jove.util.Loader;
import org.sarge.jove.util.JoveImage;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Loader for {@link HeightMap} data.
 * @author Sarge
 */
public class HeightMapLoader implements Loader<HeightMap> {
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
	@Override
	public HeightMap load( String path ) throws IOException {
		final JoveImage image = loader.load( path );
		return create( image );
	}

	/**
	 * Constructs a height-map from the given gray-scale image.
	 * Assumes RGB format with all three bytes the same height value.
	 * @param image Gray-scale image
	 * @return Height map
	 * @throws IllegalArgumentException if the image is not a gray-scale
	 */
	public static HeightMap create( JoveImage image ) {
		// Verify image
		if( image.hasAlpha() ) throw new IllegalArgumentException( "Not a gray-scale image" );

		// Get dimensions
		final Dimensions size = image.getDimensions();
		final int w = size.getWidth();
		final int h = size.getHeight();

		// Create raw height-map
		final float[][] map = new float[ w ][ h ];

		// Convert image to height-map
		final ByteBuffer buffer = image.getBuffer();
		for( int x = 0; x < w; ++x ) {
			for( int y = 0; y < h; ++y ) {
				map[ x ][ y ] = buffer.get();
				buffer.get();
				buffer.get();
			}
		}

		// Create height-map
		return new MutableHeightMap( map );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
