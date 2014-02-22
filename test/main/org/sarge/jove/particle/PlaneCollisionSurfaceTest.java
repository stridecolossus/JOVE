package org.sarge.jove.particle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Plane;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class PlaneCollisionSurfaceTest {
	private Plane plane;
	private CollisionSurface surface;

	@Before
	public void before() {
		plane = new Plane( Vector.Y_AXIS, -5 );
		surface = new PlaneCollisionSurface( plane );
	}

	@Test
	public void intersects() {
		// No intersection in-front of plane
		final Particle p = new Particle( new Point( 0, 10, 0 ), Vector.Y_AXIS.invert(), null, 0 );
		assertFalse( surface.intersects( p ) );

		// Intersects on plane
		p.setPosition( new Point( 0, 5, 0 ) );
		assertTrue( surface.intersects( p ) );

		// Intersects behind plane
		p.setPosition( new Point( 0, 0, 0 ) );
		assertTrue( surface.intersects( p ) );

		// No intersection if facing in same direction as the plane
		p.setDirection( Vector.Y_AXIS );
		assertFalse( surface.intersects( p ) );
	}

	@Test
	public void reflect() {
		final Vector v = new Vector( 1, 2, 3 );
		assertEquals( new Vector( 1, -2, 3 ), surface.reflect( v ) );
	}
}
