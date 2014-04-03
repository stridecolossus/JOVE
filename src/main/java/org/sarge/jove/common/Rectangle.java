package org.sarge.jove.common;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;

/**
 * 2D rectangle.
 * @author Sarge
 */
public class Rectangle {
	private final Location loc;
	private final Dimensions dim;

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public Rectangle( int x, int y, int w, int h ) {
		this( new Location( x, y ), new Dimensions( w, h ) );
	}

	/**
	 * Constructor.
	 * @param loc
	 * @param dim
	 */
	public Rectangle( Location loc, Dimensions dim ) {
		Check.notNull( loc );
		Check.notNull( dim );
		this.loc = loc;
		this.dim = dim;
	}

	/**
	 * Constructor at origin.
	 * @param dim Width-height
	 */
	public Rectangle( Dimensions dim ) {
		this( new Location(), dim );
	}

	public int getX() {
		return loc.x;
	}

	public int getY() {
		return loc.y;
	}

	public int getWidth() {
		return dim.w;
	}

	public int getHeight() {
		return dim.h;
	}

	public Location getLocation() {
		return loc;
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
		return loc + "(" + dim + ")";
	}
}
