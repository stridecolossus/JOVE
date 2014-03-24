package org.sarge.jove.terrain;

import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Location;

public class DepositionTerrainModifierTest {
	private DepositionTerrainModifier mod;
	private MutableHeightMap map;

	@Before
	public void before() {
		mod = new DepositionTerrainModifier( 1 );
		map = new MutableHeightMap( 5, 5 );
	}

	@Test
	public void apply() {
		// Apply to location and check height-map incremented
		final Location loc = new Location( 1, 2 );
		mod.apply( loc, map );
		assertFloatEquals( 1, map.getHeight( loc ) );

		// Check other vertices are unaffected
		for( int x = 0; x < map.getWidth(); ++x ) {
			for( int y = 0; y < map.getHeight(); ++y ) {
				if( ( x == 1 ) && ( y == 2 ) ) continue;
				assertFloatEquals( 0, map.getHeight( x, y ) );
			}
		}
	}

	@Test
	public void neighbours() {
		// Apply twice to same location
		final Location loc = new Location( 1, 2 );
		mod.apply( loc, map );
		mod.apply( loc, map );

		//
		final MutableHeightMap expected = new MutableHeightMap( 5, 5 );
		expected.setHeight( 0, 1, 1f );
		expected.setHeight( 1, 1, 1f );
		expected.setHeight( 2, 1, 1f );
		expected.setHeight( 3, 1, 1f );
		expected.setHeight( 0, 1, 1f );
		expected.setHeight( 0, 1, 1f );
		//assertFloatEquals( 2, map.getHeight( loc ) );


	}
}
