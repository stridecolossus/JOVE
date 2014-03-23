package org.sarge.jove.common;

import org.sarge.lib.util.EqualsBuilder;

/**
 * 2D rectangle.
 * @author Sarge
 */
public class Rectangle {
	private final int x, y, w, h;

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public Rectangle( int x, int y, int w, int h ) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	/**
	 * Constructor given dimensions.
	 * @param dim Width-height
	 */
	public Rectangle( Dimensions dim ) {
		this( 0, 0, dim.getWidth(), dim.getHeight() );
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}

	public Dimensions getDimensions() {
		return new Dimensions( w, h );
	}

	@Override
	public boolean equals( Object obj ) {
		return EqualsBuilder.equals( this, obj );
	}

	@Override
	public String toString() {
		return x + "," + y + "(" + w + "x" + h + ")";
	}
}
