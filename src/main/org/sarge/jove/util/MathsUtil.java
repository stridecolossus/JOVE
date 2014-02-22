package org.sarge.jove.util;

import org.sarge.lib.util.Check;

/**
 * Fast maths operations.
 * @author Sarge
 */
public class MathsUtil {
	/**
	 * PI as floating-point.
	 */
	public static final float PI = (float) Math.PI;

	/**
	 * Half-PI.
	 */
	public static final float HALF_PI = PI / 2;

	/**
	 * Double PI.
	 */
	public static final float TWO_PI = 2 * PI;

	/**
	 * Degrees in a half-circle.
	 */
	public static final float HALF_CIRCLE_DEGREES = 180;

	/**
	 * Converts degrees to radians.
	 */
    public static final float DEGREES_TO_RADIANS = PI / HALF_CIRCLE_DEGREES;

    /**
     * Converts radians to degrees.
     */
    public static final float RADIANS_TO_DEGREES = HALF_CIRCLE_DEGREES / PI;

    /**
     * Floating-point value close to zero.
     */
    public static final float EPSILON = 0.00001f;

    /**
     * Half integer.
     */
	public static final float HALF = 0.5f;

	private MathsUtil() {
		// Utility class
	}

	/**
	 * @param f Value
	 * @return Square-root
	 */
	public static float sqrt( float f ) {
		return (float) Math.sqrt( f );
	}

	/**
	 * @param num
	 * @return Whether the given number is a power-of-two
	 */
	public static boolean isPowerOfTwo( int num ) {
        return ( num > 0 ) && ( num & ( num - 1 ) ) == 0;
    }

	/**
	 * @param f Value to test
	 * @return Whether the given value is close-to-zero
	 * @see #EPSILON
	 */
	public static boolean isZero( float f ) {
		return Math.abs( f ) < EPSILON;
	}

	/**
	 * @param a
	 * @param b
	 * @return Whether two floating-point values are roughly equal
	 */
	public static boolean isEqual( float a, float b ) {
		return Math.abs( a - b ) < EPSILON;
	}

	/**
	 * Clamps a value to the given range.
	 * @param value Floating-point value
	 * @return Clamped value
	 */
	public static float clamp( float value, float min, float max ) {
		if( value < min ) return min;
		if( value > max ) return max;
		return value;
	}

	/**
	 * Clamps a value to the 0..1 range.
	 * @param value Floating-point value
	 * @return Clamped value
	 */
	public static float clamp( float value ) {
		return clamp( value, 0, 1 );
	}

	/**
	 * @param num Number
	 * @return Whether the given number is even
	 */
	public static boolean isEven( int n ) {
		return ( n % 2 ) == 0;
	}

	/**
	 * Converts a comma-delimited string to an array.
	 * @param str Comma-delimited array
	 * @param len Expected length
	 * @return Floating-point array
	 * @throws NumberFormatException if the string is not a valid floating-point array
	 */
	public static float[] convert( String str, int len ) {
		Check.notEmpty( str );
		Check.oneOrMore( len );

		// Tokenize string
		final String[] parts = str.trim().split( "," );
		if( parts.length != len ) throw new IllegalArgumentException( "Expected array of length " + len );

		// Convert to array
		final float[] array = new float[ len ];
		for( int n = 0; n < len; ++n ) {
			array[ n ] = Float.valueOf( parts[ n ].trim() );
		}

		return array;
	}

	/**
	 * Linear interpolation.
	 * @param value		Percentile
	 * @param start		Start value
	 * @param end		End value
	 * @return Interpolated value
	 */
	public static float lerp( float value, float start, float end ) {
		Check.range( value, 0, 1 );
		return ( start * ( 1 - value ) ) + ( end * value );
	}

	/**
	 * Converts an angle in radians to degrees.
	 * @param radians Angle
	 * @return Angle in degrees
	 */
	public static float toDegrees( float radians ) {
		return radians * RADIANS_TO_DEGREES;
	}

	/**
	 * Converts an angle in degrees to radians.
	 * @param degrees Angle
	 * @return Angle in radians
	 */
	public static float toRadians( float degrees ) {
		return degrees * DEGREES_TO_RADIANS;
	}

	/**
	 * Ensures the given angle is within the safe range on x86 processors.
	 * @param angle Angle (radians)
	 * @return Sine of the given angle
	 */
	private static float constrainSinAngle( float angle ) {
		// Clamp angle to two-PI space
		float result = angle % TWO_PI;

		// Clamp to PI space
		if( Math.abs( result ) > PI ) {
			result = result - TWO_PI;
		}

		// Clamp to half-PI space
		if( Math.abs( result ) > HALF_PI ) {
			result = PI - result;
		}

		return result;
	}

	/**
	 * @param angle Angle (radians)
	 * @return Sine of the given angle
	 */
	public static float sin( float angle ) {
		float reduced = constrainSinAngle( angle );

	    if( Math.abs( reduced ) <= Math.PI / 4 ) {
	    	return (float) Math.sin( reduced );
	    }
	    else {
	    	return (float) Math.cos( Math.PI / 2 - reduced );
	    }
	}

	/**
	 * @param angle Angle (radians)
	 * @return Cosine of the given angle
	 */
	public static float cos( float angle ) {
		return sin( angle + HALF_PI );
	}

	/**
	 * @param angle Angle (radians)
	 * @return Tangent of the given angle
	 */
	public static float tan( float angle ) {
		return (float) Math.tan( angle );
	}

	/**
	 * @param angle Angle (radians)
	 * @return Arc-sine of the given angle
	 */
	public static float asin( float angle ) {
	    if( -1.0f < angle ) {
	        if( angle < 1.0f ) {
	            return (float) Math.asin( angle );
	        }
	        else {
	        	return HALF_PI;
	        }
	    }
	    else {
	    	return -HALF_PI;
	    }
	}

	/**
	 * @param angle Angle (radians)
	 * @return Arc-cosine of the given angle
	 */
	public static float acos( float angle ) {
		if( -1.0f < angle ) {
			if( angle < 1.0f ) {
				return (float) Math.acos( angle );
			}
			else {
				return 0.0f;
			}
		}
		else {
			return PI;
		}
	}

	/**
	 * @param angle Angle (radians)
	 * @return Arc-tangent of the given angle
	 */
	public static float atan( float angle ) {
		return (float) Math.atan( angle );
	}
}
