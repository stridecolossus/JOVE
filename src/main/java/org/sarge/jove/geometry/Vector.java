package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Converter;

/**
 * Immutable vector or direction.
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
	public static final Converter<Vector> CONVERTER = new Converter<Vector>() {
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
	public final float getMagnitudeSquared() {
		return x * x + y * y + z * z;
	}

	/**
	 * Calculates the angle between this and the given vector (assumes both normalised).
	 * @param vec Vector
	 * @return Angle between vectors (radians)
	 */
	public final float angle( Vector vec ) {
		final float dot = dot( vec );
		if( dot < -1 ) {
			return -1;
		}
		else
		if( dot > 1 ) {
			return 1;
		}
		else {
			return MathsUtil.acos( dot );
		}
	}

	/**
	 * @return Vector instance for mutators
	 */
	@SuppressWarnings("unchecked")
	protected <V extends Vector> V getResultVector() {
		return (V) new Vector( x, y, z );
	}

	/**
	 * @return Normalised vector
	 */
	@SuppressWarnings("unchecked")
	public <V extends Vector> V normalize() {
		// Calc length
		final float len = getMagnitudeSquared();

		// Skip if already normalised
		if( MathsUtil.isEqual( len, 1 ) ) return (V) this;

		// Normalise
		return multiply( 1f / MathsUtil.sqrt( len ) );
	}

	/**
	 * Adds a vector to this vector.
	 * @param vec Vector to add
	 * @return New vector
	 */
	public <V extends Vector> V add( Vector vec ) {
		final V result = getResultVector();
		result.x = this.x + vec.x;
		result.y = this.y + vec.y;
		result.z = this.z + vec.z;
		return result;
	}

	/**
	 * Subtracts the given vector from this vector.
	 * @param vec Vector to subtract
	 * @return Subtracted vector
	 */
	public <V extends Vector> V subtract( Vector vec ) {
		final V result = getResultVector();
		result.x = vec.x - this.x;
		result.y = vec.y - this.y;
		result.z = vec.z - this.z;
		return result;
	}

	/**
	 * Multiplies this vector.
	 * @param scale Scalar
	 * @return Scaled vector
	 */
	public <V extends Vector> V multiply( float scale ) {
		final V result = getResultVector();
		result.x = this.x * scale;
		result.y = this.y * scale;
		result.z = this.z * scale;
		return result;
	}

	/**
	 * Computes the cross-product of this and the given vector.
	 * @param vec Vector
	 * @return Cross-product
	 */
	public <V extends Vector> V cross( Vector vec ) {
		// Calc temporary values (cross uses all XYZ components)
		final float dx = this.y * vec.z - this.z * vec.y;
		final float dy = this.z * vec.x - this.x * vec.z;
		final float dz = this.x * vec.y - this.y * vec.x;

		// Set result
		final V result = getResultVector();
		result.x = dx;
		result.y = dy;
		result.z = dz;
		return result;
	}

	/**
	 * @return Inverted vector
	 */
	public <V extends Vector> V invert() {
		final V result = getResultVector();
		result.x = -this.x;
		result.y = -this.y;
		result.z = -this.z;
		return result;
	}

	/**
	 * Reflects this vector about the given normal.
	 * @param normal Normal
	 * @return This reflected vector
	 */
	public <V extends Vector> V reflect( Vector normal ) {
		final float f = this.dot( normal ) * -2f;
		return normal.multiply( f ).add( this );
	}
}
