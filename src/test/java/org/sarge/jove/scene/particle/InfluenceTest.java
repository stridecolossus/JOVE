package org.sarge.jove.scene.particle;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

class InfluenceTest {
	private Particle particle;

	@BeforeEach
	void before() {
		particle = new Particle(0, Point.ORIGIN, Axis.Y);
	}

//	@Test
//	void vector() {
//		final Influence inf = Influence.of(Axis.X);
//		inf.apply(particle, 1);
//		assertEquals(Axis.X.add(Axis.Y), particle.direction());
//	}

	@Test
	void velocity() {
		final Influence inf = Influence.velocity(2);
		inf.apply(particle, 1);
//		assertEquals(2, particle.length());
		// TODO
	}
}
