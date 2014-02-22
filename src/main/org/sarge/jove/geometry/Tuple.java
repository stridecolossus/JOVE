package org.sarge.jove.geometry;

import java.nio.FloatBuffer;

import org.sarge.jove.common.Appendable;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Common base-class for 3D points and vectors.
 * @author Sarge
 */
public abstract class Tuple implements Appendable {
	/**
	 * Number of values in a point.
	 */
	public static final int SIZE = 3;

	protected float x, y, z;

	/**
	 * Origin constructor.
	 */
	protected Tuple() {
		this( 0, 0, 0 );
	}

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param z
	 */
	protected Tuple( float x, float y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Constructor given an array.
	 * @param array
	 */
	protected Tuple( float[] array ) {
		Check.notNull( array );
		if( array.length < 3 ) throw new IllegalArgumentException( "Expected tuple array" );
		this.x = array[0];
		this.y = array[1];
		this.z = array[2];
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	/**
	 * Calculates the dot (or scalar) product of this and the given tuple.
	 * @param t Tuple
	 * @return Dot product
	 */
	public float dot( Tuple t ) {
		return x * t.x + y * t.y + z * t.z;
	}

	@Override
	public void append( FloatBuffer buffer ) {
		buffer.put( x );
		buffer.put( y );
		buffer.put( z );
	}

	/**
	 * Stores to the given array.
	 * @param array
	 */
	public void toArray( float[] array ) {
		array[0] = x;
		array[1] = y;
		array[2] = z;
	}

	@Override
	public boolean equals( Object obj ) {
		if( obj == this ) return true;
		if( obj == null ) return false;
		if( obj instanceof Tuple ) {
			final Tuple t = (Tuple) obj;
			if( !MathsUtil.isEqual( x, t.x ) ) return false;
			if( !MathsUtil.isEqual( y, t.y ) ) return false;
			if( !MathsUtil.isEqual( z, t.z ) ) return false;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return ToString.toString( x, y, z );
	}
}
