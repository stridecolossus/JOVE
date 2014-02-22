package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Plane;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.geometry.Plane.Side;

public class PlaneTest {
	private Plane plane;
	
	@Before
	public void before() {
		plane = new Plane( new Vector( 0, 1, 0 ), -5 );
	}
	
	@Test
	public void constructor() {
		assertEquals( new Vector( 0, 1, 0 ), plane.getNormal() );
		assertEquals( -5, plane.getDistance(), 0.0001f );
	}
	
	@Test
	public void distanceTo() {
		assertEquals( -5, plane.distanceTo( new Point( 0, 0, 0 ) ), 0.0001f );
		assertEquals( 0, plane.distanceTo( new Point( 0, 5, 0 ) ), 0.0001f );
		assertEquals( 5, plane.distanceTo( new Point( 0, 10, 0 ) ), 0.0001f );
	}
	
	@Test
	public void getSide() {
		assertEquals( Side.BACK, plane.getSide( new Point( 0, 0, 0 ) ) );
		assertEquals( Side.INTERSECT, plane.getSide( new Point( 0, 5, 0 ) ) );
		assertEquals( Side.FRONT, plane.getSide( new Point( 0, 10, 0 ) ) );
	}
	
	@Test
	public void constructorNormalPoint() {
		plane = new Plane( new Vector( 0, 1, 0 ), new Point( 0, 5, 0 ) );
		constructor();
	}
	
	@Test
	public void constructorPlanePoints() {
		plane = new Plane( new Point( 1, 5, 0 ), new Point( 0, 5, 0 ), new Point( 0, 5, 1 ) );
		constructor();
	}
}
