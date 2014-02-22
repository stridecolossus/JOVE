package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Converter;

/**
 * Point in 3D space.
 * @author Sarge
 */
public class Point extends Tuple {
	/**
	 * Origin.
	 */
	public static final Point ORIGIN = new Point();

	/**
	 * String-to-point converter.
	 */
	public static Converter<Point> CONVERTER = new Converter<Point>() {
		@Override
		public Point convert( String str ) throws NumberFormatException {
			final float[] array = MathsUtil.convert( str, SIZE );
			return new Point( array );
		}
	};

	/**
	 * Origin constructor.
	 */
	public Point() {
		super();
	}

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param z
	 */
	public Point( float x, float y, float z ) {
		super( x, y, z );
	}

	/**
	 * Array constructor.
	 * @param array Point array
	 */
	public Point( float[] array ) {
		super( array );
	}

	/**
	 * @param pt Point
	 * @return Distance squared to the given point
	 */
	public float distanceSquared( Point pt ) {
		final float dx = pt.x - x;
		final float dy = pt.y - y;
		final float dz = pt.z - z;
		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * Translates this point by the given vector.
	 * @param vec Translation vector
	 * @return Translated point
	 */
	public Point add( Tuple vec ) {
		return new Point(
			x + vec.x,
			y + vec.y,
			z + vec.z
		);
	}

	/**
	 * @param t Tuple
	 * @return Vector from this position to the given position
	 * TODO - should this really be here?
	 */
	public Vector subtract( Tuple t ) {
		return new Vector(
			t.x - x,
			t.y - y,
			t.z - z
		);
	}

	/**
	 * Scales this point.
	 * @param scale
	 * @return
	 */
	public Point multiply( float scale ) {
		return new Point( x * scale, y * scale, z * scale );
	}
}
