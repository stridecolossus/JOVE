package org.sarge.jove.geometry;

import java.util.Arrays;
import java.util.Collection;

import org.sarge.jove.util.MathsUtil;

/**
 * Axis-aligned bounds specified by a centre point and extents in each axis.
 * TODO
 * - min/max coords? store as member?
 * - get positive/negative corners?
 * @author Sarge
 */
public class BoundingBox implements BoundingVolume {
	private final Point centre;
	private final Vector extents;

	/**
	 * Constructor given min/max points.
	 * @param min Minimum coordinate
	 * @param max Maximum coordinate
	 */
	public BoundingBox( Point min, Point max ) {
		this( Arrays.asList( min, max ) );
	}

	/**
	 * Constructor given a set of points.
	 * @param points Points
	 */
	public BoundingBox( Collection<Point> points ) {
		// Init min/max
		final float[] min = init( Float.MAX_VALUE );
		final float[] max = init( Float.MIN_VALUE );

		// Calculate min/max bounds
		final float[] array = new float[ 3 ];
		for( Point pos : points ) {
			pos.toArray( array );
			for( int n = 0; n < 3; ++n ) {
				final float f = array[ n ];
				if( f < min[ n ] ) {
					min[ n ] = f;
				}
				if( f > max[ n ] ) {
					max[ n ] = f;
				}
			}
		}

		// Derive extents and centre point
		final float[] ext = new float[ 3 ];
		final float[] mid = new float[ 3 ];
		for( int n = 0; n < 3; ++n ) {
			ext[ n ] = ( max[ n ] - min[ n ] ) * MathsUtil.HALF;
			mid[ n ] = min[ n ] + ext[ n ];
		}

		// Convert to box
		this.centre = new MutablePoint( new Point( mid ) );
		this.extents = new MutableVector( new Vector( ext ) );
	}

	private static float[] init( float f ) {
		final float[] array = new float[ 3 ];
		Arrays.fill( array, f );
		return array;
	}

	@Override
	public Point getCentre() {
		return centre;
	}

	/**
	 * @return Extents in each axis
	 */
	public Vector getExtents() {
		return extents;
	}

	/**
	 * @return Minimum point of this box
	 */
	public Point getMinimum() {
		return new Point(
			centre.x - extents.x,
			centre.y - extents.y,
			centre.z - extents.z
		);
	}

	/**
	 * @return Minimum point of this box
	 */
	public Point getMaximum() {
		return new Point(
			centre.x + extents.x,
			centre.y + extents.y,
			centre.z + extents.z
		);
	}

	/**
	 * @param pt Point to test
	 * @return Whether this bounding box contains the given point
	 */
	@Override
	public boolean contains( Point pt ) {
		if( !contains( pt.x, centre.x, extents.x ) ) return false;
		if( !contains( pt.y, centre.y, extents.y ) ) return false;
		if( !contains( pt.z, centre.z, extents.z ) ) return false;
		return true;
	}

	private static boolean contains( float value, float centre, float extent ) {
		return Math.abs( centre - value ) <= extent;
	}

	@Override
	public boolean intersects( Ray ray ) {
		// TODO
		return false;
	}

	@Override
	public String toString() {
		return centre + "(" + extents + ")";
	}
}
