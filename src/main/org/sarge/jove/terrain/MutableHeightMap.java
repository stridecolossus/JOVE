package org.sarge.jove.terrain;

import java.awt.image.BufferedImage;

import org.sarge.jove.common.Location;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * Mutable implementation.
 * @author Sarge
 */
public class MutableHeightMap implements HeightMap {
	private final float[][] map;

	/**
	 * Constructor for a flat map.
	 * @param w Width
	 * @param h Height
	 */
	public MutableHeightMap( int w, int h ) {
		Check.oneOrMore( w );
		Check.oneOrMore( h );
		this.map = new float[ w ][ h ];
	}

	/**
	 * Constructor given a 2D array.
	 * @param map Height-map
	 */
	public MutableHeightMap( float[][] map ) {
		Check.oneOrMore( map.length );
		Check.oneOrMore( map[ 0 ].length );
		this.map = map.clone();
	}

	/**
	 * Constructor given a gray-scale image.
	 * @param image Gray-scale image
	 * @return Height map
	 * @throws IllegalArgumentException if the image is not a gray-scale
	 */
	public MutableHeightMap( BufferedImage image ) {
		// Verify image
//		if( image.getType() != BufferedImage.TYPE_BYTE_GRAY ) throw new IllegalArgumentException( "Not a gray-scale image" );

		// Create height-map
		final int w = image.getWidth();
		final int h = image.getHeight();
		this.map = new float[ w ][ h ];

		// Convert image to height-map
		for( int x = 0; x < w; ++x ) {
			for( int y = 0; y < h; ++y ) {
				map[ x ][ y ] = ( image.getRGB( x, y ) >> 16 ) & 0xFF;
			}
		}
	}

	@Override
	public int getWidth() {
		return map.length;
	}

	@Override
	public int getHeight() {
		return map[ 0 ].length;
	}

	@Override
	public float getHeight( int x, int y ) {
		return map[ x ][ y ];
	}

	public float getHeight( Location loc ) {
		return getHeight( loc.getX(), loc.getY() );
	}

	public boolean contains( Location loc ) {
		if( !contains( loc.getX(), getWidth() ) ) return false;
		if( !contains( loc.getY(), getHeight() ) ) return false;
		return true;
	}

	private static boolean contains( int coord, int max ) {
		if( coord < 0 ) return false;
		if( coord > max ) return false;
		return true;
	}

	/**
	 * Sets the height at the given coordinate.
	 * @param x
	 * @param y
	 * @param h Height
	 */
	public void setHeight( int x, int y, float h ) {
		map[ x ][ y ] = h;
	}

	/**
	 * Increments the height-map at the given location.
	 * @param loc Location
	 * @param inc Height increment
	 */
	public void increment( Location loc, float inc ) {
		map[ loc.getX() ][ loc.getY() ] += inc;
	}

	@Override
	public boolean equals( Object obj ) {
		if( obj == this ) return true;
		if( obj == null ) return false;
		if( obj instanceof MutableHeightMap ) {
			// Compare dimensions
			final MutableHeightMap m = (MutableHeightMap) obj;
			if( this.getWidth() != m.getWidth() ) return false;
			if( this.getHeight() != m.getHeight() ) return false;

			// Compare height-map
			for( int x = 0; x < getWidth(); ++x ) {
				for( int y = 0; y < getHeight(); ++y ) {
					if( !MathsUtil.isEqual( this.getHeight( x, y ), m.getHeight( x, y ) ) ) return false;
				}
			}

			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return getWidth() + "," + getHeight();
	}
}
