package org.sarge.jove.particle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import org.junit.Test;
import org.sarge.jove.geometry.Vector;

public class ConeDirectionFactoryTest {
	@Test
	public void before() {
		final ConeDirectionFactory factory = new ConeDirectionFactory( new Vector( 0, 1, 0 ), 0.5f );
		final Vector dir = factory.getDirection();
		assertNotNull( dir );
		assertFloatEquals( 1, dir.getY() );
		assertEquals( true, ( dir.getX() >= -0.5f ) && ( dir.getX() <= 0.5f ) );
		assertEquals( true, ( dir.getZ() >= -0.5f ) && ( dir.getZ() <= 0.5f ) );
	}
}
