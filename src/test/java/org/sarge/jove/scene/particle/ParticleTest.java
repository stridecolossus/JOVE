package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.*;
import org.sarge.jove.scene.particle.Particle;

public class ParticleTest {
	private Particle particle;

	@BeforeEach
	void before() {
		particle = new Particle(1, Point.ORIGIN, Axis.Y);
	}

	@DisplayName("A new particle has an initial position and movement vector")
	@Test
	void constructor() {
		assertEquals(1, particle.created());
		assertEquals(Point.ORIGIN, particle.origin());
		assertEquals(Axis.Y, particle.direction());
		assertEquals(true, particle.isAlive());
		assertEquals(false, particle.isIdle());
	}

	@DisplayName("A particle can be moved")
	@Test
	void move() {
		particle.move(Axis.X);
		assertEquals(new Point(1, 0, 0), particle.origin());
	}

	@DisplayName("The movement vector of a particle can be combined")
	@Test
	void vector() {
		particle.add(Axis.X);
		assertEquals(Axis.X.add(Axis.Y), particle.direction());
	}

	@DisplayName("The velocity of a particle can be modified")
	@Test
	void velocity() {
		particle.velocity(2);
		assertEquals(Axis.Y.multiply(2), particle.direction());
	}

	@DisplayName("A moving particle can be stopped")
	@Test
	void stop() {
		particle.stop();
		assertEquals(true, particle.isIdle());
	}

	@DisplayName("A particle cannot be stopped more than once")
	@Test
	void stopped() {
		particle.stop();
		assertThrows(IllegalStateException.class, () -> particle.stop());
	}

	@DisplayName("An active particle can be destroyed")
	@Test
	void kill() {
		particle.destroy();
		assertEquals(false, particle.isAlive());
	}

	@DisplayName("A particle cannot be destroyed more than once")
	@Test
	void destroyed() {
		particle.destroy();
		assertThrows(IllegalStateException.class, () -> particle.destroy());
	}

	@DisplayName("A particle can be reflected about an intersection")
	@Test
	void reflect() {
		final Point pt = new Point(1, 2, 3);
		particle.reflect(pt, Axis.Y);
		assertEquals(pt, particle.origin());
		assertEquals(Axis.Y.invert(), particle.direction());
	}

	@DisplayName("TODO")
	@Test
	void colour() {
		particle.colour(Colour.BLACK);
	}

	@DisplayName("TODO")
	@Test
	void buffer() {
		// TODO
		//particle.buffer(mock)
	}
}
