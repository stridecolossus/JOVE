package org.sarge.jove.model;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.ToString;

/**
 * Unit-circle builder.
 * @author Sarge
 */
public class UnitCircle {
	/**
	 * Point on the 2D unit-circle.
	 */
	public static class CirclePoint {
		private final float x, y;

		private CirclePoint( float x, float y ) {
			this.x = x;
			this.y = y;
		}

		public float getX() {
			return x;
		}

		public float getY() {
			return y;
		}

		@Override
		public String toString() {
			return x + "," + y;
		}
	}

	private final CirclePoint[] points;

	/**
	 * Constructor for a complete circle.
	 * @param num Number of points on the circle (2 or more)
	 */
	public UnitCircle( int num ) {
		this( num, 0, MathsUtil.TWO_PI );
	}

	/**
	 * Constructor for a segment.
	 * @param num		Number of points on the circle (2 or more)
	 * @param start		Start angle of segment (radians)
	 * @param end		End angle (radians)
	 */
	public UnitCircle( int num, float start, float end ) {
		if( num < 2 ) throw new IllegalArgumentException( "Unit-circle must have at least 2 points" );

		// Determine angle between points
		final float segment = ( end - start ) / num;

		// Create unit-circle segment
		this.points = new CirclePoint[ num ];
		for( int n = 0; n < num; ++n ) {
			final float angle = n * segment;
			final float x = MathsUtil.sin( angle );
			final float y = MathsUtil.cos( angle );
			final CirclePoint pt = new CirclePoint( x, y );
			points[ n ] = pt;
		}
	}

	/**
	 * @return Points on the 2D unit-circle
	 */
	public CirclePoint[] getPoints() {
		return points;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
