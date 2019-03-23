package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class ParticleTest {
	private Particle particle;
	private Vector vec;

	@BeforeEach
	public void before() {
		particle = new Particle(Point.ORIGIN);
		vec = new Vector(1, 2, 3);
	}

	@Test
	public void constructor() {
		assertEquals(Point.ORIGIN, particle.position());
		assertEquals(new Vector(0, 0, 0), particle.vector());
	}

	@Test
	public void add() {
		particle.add(vec);
		particle.add(vec);
		assertEquals(vec.add(vec), particle.vector());
	}

	@Test
	public void stop() {
		particle.add(vec);
		particle.stop();
		assertEquals(new Vector(0, 0, 0), particle.vector());
	}

	@Test
	public void update() {
		particle.add(vec);
		particle.update();
		assertEquals(new Point(vec), particle.position());
	}
}
