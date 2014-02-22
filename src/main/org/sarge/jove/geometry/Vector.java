package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Converter;

/**
 * Vector or direction.
 * @author Sarge
 */
public class Vector extends Tuple {
	/**
	 * X axis.
	 */
	public static final Vector X_AXIS = new Vector( 1, 0, 0 );

	/**
	 * Y axis.
	 */
	public static final Vector Y_AXIS = new Vector( 0, 1, 0 );

	/**
	 * Z axis.
	 */
	public static final Vector Z_AXIS = new Vector( 0, 0, 1 );

	/**
	 * String-to-vector converter.
	 */
	public static Converter<Vector> CONVERTER = new Converter<Vector>() {
		@Override
		public Vector convert( String str ) throws NumberFormatException {
			final float[] array = MathsUtil.convert( str, SIZE );
			return new Vector( array );
		}
	};

	/**
	 * Origin constructor.
	 */
	public Vector() {
		super();
	}

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector( float x, float y, float z ) {
		super( x, y, z );
	}

	/**
	 * Array constructor.
	 * @param array Vector as an array
	 */
	public Vector( float[] array ) {
		super( array );
	}

	/**
	 * @return Magnitude (or <i>length</i>) <b>squared</b> of this vector
	 */
	public float getMagnitudeSquared() {
		return x * x + y * y + z * z;
	}

	/**
	 * Adds a vector to this vector.
	 * @param v Vector to add
	 * @return Resultant vector
	 */
	public Vector add( Vector v ) {
		return new Vector(
			x + v.x,
			y + v.y,
			z + v.z
		);
	}

	/**
	 * Multiplies this vector.
	 * @param scale Scalar
	 * @return Scaled vector
	 */
	public Vector multiply( float scale ) {
		return new Vector(
			x * scale,
			y * scale,
			z * scale
		);
	}

	/**
	 * @return Inverted vector
	 */
	public Vector invert() {
		return new Vector( -x, -y, -z );
	}

	/**
	 * Normalises this vector.
	 * @return Normalised vector
	 */
	public Vector normalize() {
		// Calc length
		final float len = getMagnitudeSquared();

		// Skip if already normalised
		if( MathsUtil.isEqual( len, 1 ) ) return this;

		// Normalise
		final float inv = 1f / MathsUtil.sqrt( len );
		return new Vector( x * inv, y * inv, z * inv );
	}

	/**
	 * Computes the cross-product of this and the given vector.
	 * @param v Vector
	 * @return Cross-product
	 */
	public Vector cross( Vector b ) {
		return new Vector(
			y * b.z - z * b.y,
			z * b.x - x * b.z,
			x * b.y - y * b.x
		);
	}

	/**
	 * Calculates the angle between this and the given vector.
	 * @param v Vector
	 * @return Angle between vectors (radians)
	 */
	public float angle( Vector v ) {
		final float left = MathsUtil.sqrt( this.getMagnitudeSquared() );
		final float right = MathsUtil.sqrt( v.getMagnitudeSquared() );
		final float inv = dot( v ) / ( left * right );
		if( inv < -1 ) return -1;
		if( inv > 1 ) return 1;
		return MathsUtil.acos( inv );
	}

	/**
	 * Projects the given vector or point onto this vector (assumes this vector is normalized).
	 * @param t Vector or point
	 * @return Projected vector
	 */
	public Vector project( Tuple t ) {
		return multiply( dot( t ) );
	}

	/**
	 * Reflects this vector about the given normal.
	 * @param normal Normal
	 * @return Reflected vector
	 */
	public Vector reflect( Vector normal ) {
		return normal.multiply( this.dot( normal ) * -2f ).add( this );
	}
}
