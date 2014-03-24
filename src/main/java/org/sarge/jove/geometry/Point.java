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
	public static final Converter<Point> CONVERTER = new Converter<Point>() {
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
	public final float distanceSquared( Point pt ) {
		final float dx = pt.x - x;
		final float dy = pt.y - y;
		final float dz = pt.z - z;
		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * @return Result for mutators
	 */
	@SuppressWarnings("unchecked")
	protected <P extends Point> P getResult() {
		return (P) new Point( x, y, z );
	}

	/**
	 * Adds to this point.
	 * @param pt Point to add
	 * @return This point
	 */
	public <P extends Point> P add( Tuple t ) {
		final P result = getResult();
		result.x = this.x + t.x;
		result.y = this.y + t.y;
		result.z = this.z + t.z;
		return result;
	}

	/**
	 * Scales this point.
	 * @param scale Scalar
	 * @return This point
	 */
	public <P extends Point> P multiply( float scale ) {
		final P result = getResult();
		result.x = this.x * scale;
		result.y = this.y * scale;
		result.z = this.z * scale;
		return result;
	}


	/**
	 * Projects this point onto the given vector.
	 * @param vec Vector to project onto (assumes normalised)
	 * @return Projected point
	 */
	public <P extends Point> P project( Vector vec ) {
		final P result = getResult();
		return result.multiply( vec.dot( this ) );
	}
}
