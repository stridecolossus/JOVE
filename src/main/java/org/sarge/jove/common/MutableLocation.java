package org.sarge.jove.common;

/**
 * Mutable screen location.
 * @author Sarge
 */
public class MutableLocation extends Location {
	/**
	 * Constructor.
	 * @param x
	 * @param y
	 */
	public MutableLocation( int x, int y ) {
		super( x, y );
	}

	/**
	 * Copy constructor.
	 * @param loc
	 */
	public MutableLocation( Location loc ) {
		super( loc );
	}

	/**
	 * Origin constructor.
	 */
	public MutableLocation() {
		super();
	}

	/**
	 * Resets this location.
	 * @param x
	 * @param y
	 */
	public void set( int x, int y ) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Increments this location.
	 * @param dx
	 * @param dy
	 */
	public void add( int dx, int dy ) {
		this.x += dx;
		this.y += dy;
	}
}
