package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class SpherePositionFactoryTest {
	private PositionFactory factory;

	@BeforeEach
	public void before() {
		factory = new SphericalPositionFactory(3);
	}

	@Test
	public void generate() {
		final Point pos = factory.position();
		assertNotNull(pos);
		assertEquals(3 * 3, new Vector(pos).magnitude(), 0.0001f);
	}
}
