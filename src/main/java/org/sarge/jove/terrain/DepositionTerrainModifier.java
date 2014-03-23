package org.sarge.jove.terrain;

import org.sarge.jove.common.Location;
import org.sarge.jove.common.MutableLocation;

/**
 * Deposition terrain modifier.
 * @author Sarge
 */
public class DepositionTerrainModifier extends IncrementTerrainModifier {
	private final MutableLocation neighbour = new MutableLocation();

	public DepositionTerrainModifier( float inc ) {
		super( inc );
	}

	@Override
	public void apply( Location loc, MutableHeightMap map ) {
		// Recurse to neighbouring vertices that are lower than centre
		applyNeighbours( loc, map.getHeight( loc ), map );

		// Increment this location
		super.apply( loc, map );
	}

	/**
	 * Increments neighbouring vertices that are lower than the centre.
	 */
	private void applyNeighbours( Location loc, final float prev, MutableHeightMap map ) {
		for( int x = -1; x <= 1; ++x ) {
			for( int y = -1; y <= 1; ++y ) {
				// Skip centre
				if( ( x == 0 ) && ( y == 0 ) ) continue;

				// Skip if outside height-map
				neighbour.set( loc.getX() + x, loc.getY() + y );
				if( !map.contains( neighbour ) ) continue;

				// Increment if lower than centre and recurse
				final float h = map.getHeight( neighbour );
				if( h < prev ) {
					apply( neighbour, map );
					applyNeighbours( neighbour, h, map );
				}
			}
		}
	}
}
