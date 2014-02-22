package org.sarge.jove.terrain;

import org.sarge.jove.common.Location;

/**
 * Modifies a {@link MutableHeightMap} at a given {@link Location}.
 * @author Sarge
 */
public interface TerrainModifier {
	/**
	 * Applies this modifier to the given height-map at the specified location.
	 * @param loc Location
	 * @param map Height-map
	 */
	void apply( Location loc, MutableHeightMap map );
}
