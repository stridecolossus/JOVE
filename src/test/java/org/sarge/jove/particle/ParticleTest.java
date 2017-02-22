package org.sarge.jove.particle;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class ParticleTest {
	private Particle particle;

	@Before
	public void before() {
		particle = new Particle(new Point(1, 2, 3), new Vector(4, 5, 6), Colour.WHITE, 123);
	}

	@Test
	public void constructor() {
		assertEquals(new Point(1, 2, 3), particle.getPosition());
		assertEquals(new Vector(4, 5, 6), particle.getDirection());
		assertEquals(Colour.WHITE, particle.getColour());
		assertEquals(123L, particle.getCreationTime());
		assertFloatEquals(1, particle.getAlpha());
	}

	@Test
	public void add() {
		particle.add(new Vector(7, 8, 9));
		assertEquals(new Vector(11, 13, 15), particle.getDirection());
	}

	@Test
	public void update() {
		particle.update(1);
		assertEquals(new Point(5, 7, 9), particle.getPosition());
	}

	@Test
	public void setDirection() {
		final Vector dir = new Vector();
		particle.setDirection(dir);
		assertEquals(dir, particle.getDirection());
	}
	
	@Test
	public void setAlpha() {
		particle.setAlpha(0.5f);
		assertFloatEquals(0.5f, particle.getAlpha());
	}
}
