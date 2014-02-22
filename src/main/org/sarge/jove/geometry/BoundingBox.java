package org.sarge.jove.geometry;

import java.util.Arrays;
import java.util.Collection;

import org.sarge.lib.util.Check;

/**
 * Axis-aligned bounding box specified by min/max coordinates.
 * @author Sarge
 */
public class BoundingBox {
	private final Point min, max;

	/**
	 * Constructor.
	 * @param min Minimum coordinate
	 * @param max Maximum coordinate
	 */
	public BoundingBox( Point min, Point max ) {
		Check.notNull( min );
		Check.notNull( max );

		this.min = min;
		this.max = max;
	}

	/**
	 * Constructor given a set of points.
	 * @param points Points
	 */
	public BoundingBox( Collection<Point> points ) {
		// Init min/max
		final float[] lo = init( Float.MAX_VALUE );
		final float[] hi = init( Float.MIN_VALUE );

		// Calculate bounds
		final float[] array = new float[ 3 ];
		for( Point pos : points ) {
			pos.toArray( array );
			for( int n = 0; n < 3; ++n ) {
				final float f = array[ n ];
				if( f < lo[ n ] ) {
					lo[ n ] = f;
				}
				else
				if( f > hi[ n ] ) {
					hi[ n ] = f;
				}
			}
		}

		// Convert to bounding box
		this.min = new Point( lo );
		this.max = new Point( hi );
	}

	private static float[] init( float f ) {
		final float[] array = new float[ 3 ];
		Arrays.fill( array, f );
		return array;
	}

	/**
	 * @return Minimum coordinate
	 */
	public Point getMin() {
		return min;
	}

	/**
	 * @return Maximum coordinate
	 */
	public Point getMax() {
		return max;
	}

	/**
	 * @param pt Point to test
	 * @return Whether this bounding box contains the given point
	 */
	public boolean contains( Point pt ) {
		if( ( pt.x < min.x ) || ( pt.x > max.x ) ) return false;
		if( ( pt.y < min.y ) || ( pt.y > max.y ) ) return false;
		if( ( pt.z < min.z ) || ( pt.z > max.z ) ) return false;
		return true;
	}

	@Override
	public String toString() {
		return min + "/" + max;
	}
}
