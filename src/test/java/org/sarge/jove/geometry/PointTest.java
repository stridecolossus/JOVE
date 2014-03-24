package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;

public class PointTest {
	protected Point pt;

	@Before
	public void before() {
		pt = new Point( 1, 2, 3 );
	}

	@Test
	public void constructor() {
		assertEquals( new Point( 1, 2, 3 ), pt );
	}

	@Test
	public void distanceSquared() {
		assertFloatEquals( 0, pt.distanceSquared( pt ) );
		assertFloatEquals( 27, pt.distanceSquared( new Point( 4, 5, 6 ) ) );
	}

	@Test
	public void add() {
		assertEquals( new Point( 2, 4, 6 ), pt.add( new Point( 1, 2, 3 ) ) );
	}

	@Test
	public void multiply() {
		assertEquals( new Point( 2, 4, 6 ), pt.multiply( 2 ) );
	}

	@Test
	public void project() {
		assertEquals( new Point( 3, 6, 9 ), pt.project( Vector.Z_AXIS ) );
	}
}
