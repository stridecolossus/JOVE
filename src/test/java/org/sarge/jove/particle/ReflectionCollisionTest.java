package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

public class ReflectionCollisionTest {
	private Collision collision;
	private Particle p;
	private Intersection intersection;

	@BeforeEach
	void before() {
		collision = new ReflectionCollision(0.5f);
		p = new Particle(0, Point.ORIGIN, new Vector(1, 1, 0));
		intersection = Intersection.of(1, Vector.Y);
	}

	@Test
	void reflect() {
		collision.collide(p, intersection);
		assertEquals(new Point(1, 1, 0), p.origin());
		assertEquals(new Vector(0.5f, -0.5f, 0), p.direction());
	}
}
