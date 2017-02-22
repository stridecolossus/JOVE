package org.sarge.jove.particle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Test;
import org.sarge.jove.geometry.Vector;

public class DirectionFactoryTest {
	@Test
	public void cone() {
		final DirectionFactory factory = DirectionFactory.cone(Vector.Y_AXIS, 0.5f);
		final Vector dir = factory.getDirection();
		assertNotNull(dir);
		assertFloatEquals(1, dir.y);
		assertEquals(true, (dir.x >= -0.5f) && (dir.x <= 0.5f));
		assertEquals(true, (dir.z >= -0.5f) && (dir.z <= 0.5f));
	}

	@Test
	public void sphere() {
		final DirectionFactory factory = DirectionFactory.sphere(2);
		final Vector dir = factory.getDirection();
		assertNotNull(dir);
		assertFloatEquals(4, dir.getMagnitudeSquared());
	}
}
