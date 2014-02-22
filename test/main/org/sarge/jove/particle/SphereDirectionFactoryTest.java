package org.sarge.jove.particle;

import static org.junit.Assert.assertNotNull;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import org.junit.Test;
import org.sarge.jove.geometry.Vector;

public class SphereDirectionFactoryTest {
	@Test
	public void getDirection() {
		final DirectionFactory factory = new SphereDirectionFactory( 1 );
		final Vector dir = factory.getDirection();
		assertNotNull( dir );
		assertFloatEquals( 1, dir.getMagnitudeSquared() );
	}
}
