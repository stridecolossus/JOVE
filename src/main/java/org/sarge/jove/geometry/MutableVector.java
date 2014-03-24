package org.sarge.jove.geometry;

/**
 * Mutable implementation used to reduce object creation.
 * @author Sarge
 */
public class MutableVector extends Vector {
	/**
	 * Origin constructor.
	 */
	public MutableVector() {
		super();
	}

	/**
	 * Copy constructor.
	 * @param vec Vector to copy
	 */
	public MutableVector( Vector vec ) {
		super( vec.x, vec.y, vec.z );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected MutableVector getResultVector() {
		return this;
	}

	/**
	 * Sets this vector.
	 */
	public MutableVector set( float x, float y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	/**
	 * Sets this vector.
	 * @param vec Vector
	 * @return This vector
	 */
	public MutableVector set( Vector vec ) {
		return set( vec.x, vec.y, vec.z );
	}

	/**
	 * Sets this as the vector between the given points.
	 * @param a Start point
	 * @param b End
	 * @return This vector
	 */
	public MutableVector subtract( Point a, Point b ) {
		x = b.x - a.x;
		y = b.y - a.y;
		z = b.z - a.z;
		return this;
	}
}
