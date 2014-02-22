package org.sarge.jove.terrain;

import org.sarge.jove.common.Location;
import org.sarge.jove.common.MutableLocation;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Applies a {@link TerrainModifier} over a specified radius.
 * @author Sarge
 */
public class RadialTerrainModifier implements TerrainModifier {
	private final TerrainModifier mod;
	private final float radius;

	/**
	 * Constructor.
	 * @param mod		Delegate terrain modifier
	 * @param radius	Radius
	 */
	private RadialTerrainModifier( TerrainModifier mod, float radius ) {
		Check.notNull( mod );
		this.mod = mod;
		this.radius = radius;
	}

	@Override
	public void apply( Location centre, MutableHeightMap map ) {
		final float radiusSquared = radius * radius;
		final int diameter = (int) ( 2 * radius );
		final int w = centre.getX() + diameter;
		final int h = centre.getY() + diameter;
		final MutableLocation loc = new MutableLocation();
		for( int x = centre.getX(); x < w; ++x ) {
			for( int y = centre.getY(); y < h; ++y ) {
				loc.set( x, y );
				if( !map.contains( loc ) ) continue;
				if( centre.distanceSquared( loc ) > radiusSquared ) continue;
				mod.apply( loc, map );
			}
		}
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
