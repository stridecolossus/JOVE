package org.sarge.jove.terrain;

import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Location;

public class IncrementTerrainModifierTest {
	private IncrementTerrainModifier mod;
	private MutableHeightMap map;

	@Before
	public void before() {
		mod = new IncrementTerrainModifier( 1 );
		map = new MutableHeightMap( 5, 5 );
	}

	@Test
	public void apply() {
		final Location loc = new Location( 1, 2 );
		mod.apply( loc, map );
		assertFloatEquals( 1, map.getHeight( loc ) );
	}

	@Test
	public void ignoreOutside() {
		final Location loc = new Location( -1, -2 );
		mod.apply( loc, map );
	}
}
