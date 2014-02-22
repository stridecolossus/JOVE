package org.sarge.jove.particle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.jove.geometry.BoundingBox;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class BoundingBoxCollisionSurfaceTest {
	@Test
	public void test() {
		// Create surface
		final BoundingBox box = new BoundingBox( new Point( -1, 0, 0 ), new Point( +1, 0, 0 ) );
		final BoundingBoxCollisionSurface surface = new BoundingBoxCollisionSurface( box );

		// Check point inside intersects
		final Particle p = new Particle( new Point(), new Vector(), null, 0 );
		assertEquals( true, surface.intersects( p ) );

		// Move outside and check no longer intersects
		p.setPosition( new Point( 2, 0, 0 ) );
		assertEquals( false, surface.intersects( p ) );
	}
}
