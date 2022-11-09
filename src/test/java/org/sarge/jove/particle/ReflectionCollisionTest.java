package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

public class ReflectionCollisionTest {
	private Collision collision;
	private Particle particle;
	private Intersection intersection;

	@BeforeEach
	void before() {
		collision = new ReflectionCollision(0.5f);
		particle = new Particle(0, Point.ORIGIN, Axis.X);
		intersection = Intersection.of(1, Axis.Y);
	}

	@Test
	void reflect() {
		collision.collide(particle, intersection);
		assertEquals(new Point(1, 0, 0), particle.origin());
		assertEquals(new Vector(0.5f, 0, 0), particle.direction());
	}
}
