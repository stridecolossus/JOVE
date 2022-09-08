package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

public class CollisionTest {
	private Particle p;
	private Intersection intersection;

	@BeforeEach
	void before() {
		p = new Particle(0, Point.ORIGIN, new Vector(1, 1, 0));
		intersection = Intersection.of(1, Axis.Y);
	}

	@Test
	void destroy() {
		Collision.DESTROY.collide(p, intersection);
		assertEquals(false, p.isAlive());
	}

	@Test
	void stop() {
		Collision.STOP.collide(p, intersection);
		assertEquals(true, p.isIdle());
	}
}
