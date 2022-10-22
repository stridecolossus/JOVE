package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

public class InfluenceTest {
	private Particle particle;

	@BeforeEach
	void before() {
		particle = new Particle(0, Point.ORIGIN, Axis.Y.vector());
	}

	@Test
	void vector() {
		final Influence inf = Influence.of(Axis.X.vector());
		inf.apply(particle, 1);
		assertEquals(Axis.X.vector().add(Axis.Y.vector()), particle.direction());
	}

	@Test
	void velocity() {
		final Influence inf = Influence.velocity(2);
		inf.apply(particle, 1);
		assertEquals(Axis.Y.vector().multiply(2) , particle.direction());
	}
}
