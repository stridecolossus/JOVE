package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

class ReflectionCollisionTest {
	private Collision collision;
	private Particle particle;
	private Intersection intersection;

	@BeforeEach
	void before() {
		collision = new ReflectionCollision(0.5f);
		particle = new Particle(0, Point.ORIGIN, Axis.X);
		intersection = Intersection.of(particle, 1, Axis.X.add(Axis.Y).normalize());
	}

	@Test
	void reflect() {
		collision.collide(particle, intersection);
		assertEquals(new Point(1, 0, 0), particle.origin());
		assertEquals(Axis.Y.invert(), particle.direction());
		assertEquals(0.5f, particle.length());
	}
}
