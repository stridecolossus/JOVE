package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import org.junit.Test;
import org.sarge.jove.model.UnitCircle.CirclePoint;
import org.sarge.jove.util.MathsUtil;

public class UnitCircleTest {
	private UnitCircle circle;

	@Test
	public void unitCircle() {
		circle = new UnitCircle( 4 );
		final CirclePoint[] pts = circle.getPoints();
		assertNotNull( pts );
		assertEquals( 4, pts.length );
		check( pts[ 0 ], 0, 1 );
		check( pts[ 1 ], 1, 0 );
		check( pts[ 2 ], 0, -1 );
		check( pts[ 3 ], -1, 0 );
	}

	@Test
	public void segment() {
		circle = new UnitCircle( 2, 0, MathsUtil.toRadians( 180 ) );
		final CirclePoint[] pts = circle.getPoints();
		assertNotNull( pts );
		assertEquals( 2, pts.length );
		check( pts[ 0 ], 0, 1 );
		check( pts[ 1 ], 1, 0 );
	}

	private static void check( CirclePoint pt, float x, float y ) {
		assertFloatEquals( x, pt.getX() );
		assertFloatEquals( y, pt.getY() );
	}
}
