package org.sarge.jove.common;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;

/**
 * 2D rectangle.
 * @author Sarge
 */
public class Rectangle {
	// TODO - Location?
	private final int x, y;
	private final Dimensions dim;

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public Rectangle( int x, int y, int w, int h ) {
		this( x, y, new Dimensions( w, h ) );
	}

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param dim
	 */
	public Rectangle( int x, int y, Dimensions dim ) {
		Check.notNull( dim );
		this.x = x;
		this.y = y;
		this.dim = dim;
	}

	/**
	 * Constructor at origin.
	 * @param dim Width-height
	 */
	public Rectangle( Dimensions dim ) {
		this( 0, 0, dim );
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return dim.getWidth();
	}

	public int getHeight() {
		return dim.getHeight();
	}

	public Dimensions getDimensions() {
		return dim;
	}

	@Override
	public boolean equals( Object obj ) {
		return EqualsBuilder.equals( this, obj );
	}

	@Override
	public String toString() {
		return x + "," + y + "(" + dim + ")";
	}
}
