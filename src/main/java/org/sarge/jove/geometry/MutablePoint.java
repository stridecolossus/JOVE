package org.sarge.jove.geometry;

/**
 * Mutable implementation to avoid creation of new objects.
 * @author Sarge
 */
public class MutablePoint extends Point {
	/**
	 * Origin constructor.
	 */
	public MutablePoint() {
		super();
	}

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param z
	 */
	public MutablePoint( float x, float y, float z ) {
		super( x, y, z );
	}

	/**
	 * Copy constructor.
	 * @param pt Point to copy
	 */
	public MutablePoint( Point pt ) {
		set( pt );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected MutablePoint getResult() {
		return this;
	}

	/**
	 * Sets this point.
	 * @param pt Point to copy
	 * @return This point
	 */
	public MutablePoint set( Point pt ) {
		return set( pt.x, pt.y, pt.z );
	}

	/**
	 * Sets this point.
	 * @param x
	 * @param y
	 * @param z
	 * @return This point
	 */
	public MutablePoint set( float x, float y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
}
