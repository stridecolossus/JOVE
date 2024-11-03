package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

class CollisionTest {
	private Particle particle;
	private Intersection intersection;

	@BeforeEach
	void before() {
		particle = new Particle(0, Point.ORIGIN, new Normal(new Vector(1, 1, 0)));
		intersection = particle.ray().intersection(1, Axis.Y);
	}

	@Test
	void destroy() {
		Collision.DESTROY.collide(particle, intersection);
		assertEquals(false, particle.isAlive());
	}

	@Test
	void stop() {
		Collision.STOP.collide(particle, intersection);
		assertEquals(true, particle.isIdle());
	}
}
