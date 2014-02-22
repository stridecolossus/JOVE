package org.sarge.jove.terrain;

import org.sarge.jove.common.Location;
import org.sarge.lib.util.ToString;

/**
 * Simple height increment modifier.
 * @see MutableHeightMap#increment(Location, float)
 * @author Sarge
 */
public class IncrementTerrainModifier implements TerrainModifier {
	private final float inc;

	/**
	 * Constructor.
	 * @param inc Height increment
	 */
	public IncrementTerrainModifier( float inc ) {
		this.inc = inc;
	}

	@Override
	public void apply( Location loc, MutableHeightMap map ) {
		if( !map.contains( loc ) ) return;
		map.increment( loc, inc );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
