package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;

public class PointTest {
	private Point pt;

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
	public void addVector() {
		final Point result = pt.add( new Vector( 4, 5, 6 ) );
		assertEquals( new Point( 5, 7, 9 ), result );
	}
}
