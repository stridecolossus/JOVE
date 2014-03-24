package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Plane.Side;

public class PlaneTest {
	private Plane plane;

	@Before
	public void before() {
		plane = new Plane( Vector.Y_AXIS, 5 );
	}

	@Test
	public void constructor() {
		assertEquals( Vector.Y_AXIS, plane.getNormal() );
		assertFloatEquals( 5, plane.getDistance() );
	}

	@Test
	public void distanceTo() {
		assertFloatEquals( -5, plane.distanceTo( new Point( 0, 0, 0 ) ) );
		assertFloatEquals( 0, plane.distanceTo( new Point( 0, 5, 0 ) ) );
		assertFloatEquals( 5, plane.distanceTo( new Point( 0, 10, 0 ) ) );
	}

	@Test
	public void getSide() {
		assertEquals( Side.BACK, plane.getSide( new Point( 0, 0, 0 ) ) );
		assertEquals( Side.INTERSECT, plane.getSide( new Point( 0, 5, 0 ) ) );
		assertEquals( Side.FRONT, plane.getSide( new Point( 0, 10, 0 ) ) );
	}

	@Test
	public void intersect() {
		// Check ray above plane
		assertEquals( new Point( 0, 5, 0 ), plane.intersect( new Ray( new Point( 0, 10, 0 ), Vector.Y_AXIS.invert() ) ) );

		// Check ray above but facing away
		assertEquals( null, plane.intersect( new Ray( new Point( 0, 10, 0 ), Vector.Y_AXIS ) ) );

		// Check ray below
		assertEquals( new Point( 0, 5, 0 ), plane.intersect( new Ray( new Point( 0, 0, 0 ), Vector.Y_AXIS ) ) );

		// Check ray below but facing away
		assertEquals( null, plane.intersect( new Ray( new Point( 0, 0, 0 ), Vector.Y_AXIS.invert() ) ) );
	}

	@Test
	public void constructorNormalPoint() {
		plane = new Plane( new Vector( 0, 1, 0 ), new Point( 0, -5, 0 ) );
		constructor();
	}

	@Test
	public void constructorPlanePoints() {
		plane = new Plane( new Point( 1, -5, 0 ), new Point( 0, -5, 0 ), new Point( 0, -5, 1 ) );
		constructor();
	}
}
