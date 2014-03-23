package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;

public class SphereVolumeTest {
	private SphereVolume sphere;

	@Before
	public void before() {
		sphere = new SphereVolume( new Point( 1, 2, 3 ), 5 );
	}

	@Test
	public void constructor() {
		assertEquals( new Point( 1, 2, 3 ), sphere.getCentre() );
		assertFloatEquals( 5, sphere.getRadius() );
	}

	@Test
	public void contains() {
		assertTrue( sphere.contains( sphere.getCentre() ) );
		assertTrue( sphere.contains( new Point( 1 + 5, 2, 3 ) ) );
		assertFalse( sphere.contains( new Point( 2 + 5, 2, 3 ) ) );
	}

	@Test
	public void intersectsRay() {
		// Check intersects at origin
		assertTrue( sphere.intersects( new Ray( new Point( 1, 2, 3 ), Vector.X_AXIS ) ) );

		// Check intersects when behind ray origin
		assertTrue( sphere.intersects( new Ray( new Point( -2, 2, 3 ), Vector.X_AXIS ) ) );

		// Check intersects ahead of ray origin
		assertTrue( sphere.intersects( new Ray( new Point( +2, 2, 3 ), Vector.X_AXIS ) ) );

		// Check intersects on edge
		assertTrue( sphere.intersects( new Ray( new Point( 1, 2 + 5, 3 ), Vector.X_AXIS ) ) );

		// Check does not intersect
		assertFalse( sphere.intersects( new Ray( new Point( -2, 2 + 5, 3 ), Vector.X_AXIS ) ) );
	}
}
