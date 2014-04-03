package org.sarge.jove.common;

import org.sarge.jove.util.DefaultObjectPool;
import org.sarge.jove.util.ObjectPool;
import org.sarge.lib.util.EqualsBuilder;

/**
 * 2D integer coordinates.
 * @author Sarge
 */
public class Location {
	/**
	 * Pool for locations.
	 */
	public static final ObjectPool<Location> POOL = new DefaultObjectPool<Location>() {
		@Override
		protected Location create() {
			return new Location();
		}
	};

	/**
	 * Get a new location from the pool.
	 * @param x
	 * @param y
	 * @return Location
	 */
	public static Location get( int x, int y ) {
		final Location loc = POOL.get();
		loc.x = x;
		loc.y = y;
		return loc;
	}

	protected int x, y;

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 */
	public Location( int x, int y ) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Copy constructor.
	 * @param loc
	 */
	public Location( Location loc ) {
		this( loc.x, loc.y );
	}

	/**
	 * Origin constructor.
	 */
	public Location() {
		this( 0, 0 );
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	/**
	 * @param loc Location
	 * @return Distance-squared to the given location
	 */
	public float distanceSquared( Location loc ) {
		final float dx = this.x - loc.x;
		final float dy = this.y - loc.y;
		return dx * dx + dy * dy;
	}

	@Override
	public boolean equals( Object obj ) {
		return EqualsBuilder.equals( this, obj );
	}

	@Override
	public String toString() {
		return x + "," + y;
	}
}
