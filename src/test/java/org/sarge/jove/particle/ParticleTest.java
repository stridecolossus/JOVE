package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

class ParticleTest {
	private Particle particle;

	@BeforeEach
	void before() {
		particle = new Particle(Point.ORIGIN, Vector.Y);
	}

	@Test
	void length() {
		final ByteBuffer bb = ByteBuffer.allocate(3 * 4);
		assertEquals(3 * Float.BYTES, particle.length());
		particle.buffer(bb);
		assertEquals(0, bb.remaining());
	}

	@Test
	void equals() {
		assertEquals(particle, particle);
		assertEquals(particle, new Particle(Point.ORIGIN, Vector.Y));
		assertNotEquals(particle, null);
		assertNotEquals(particle, new Particle(Point.ORIGIN, Vector.Z));
	}

	@DisplayName("A new particle...")
	@Nested
	class New {
		@DisplayName("has an initial position and vector")
		@Test
		void constructor() {
			assertEquals(Point.ORIGIN, particle.position());
			assertEquals(false, particle.isStopped());
		}

		@DisplayName("can have its vector modified")
		@Test
		void add() {
			particle.add(Vector.X);
			particle.update();
			assertEquals(new Point(1, 1, 0), particle.position());
		}

		@DisplayName("can update its position")
		@Test
		void update() {
			particle.update();
			assertEquals(new Point(Vector.Y), particle.position());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			particle.stop();
			assertEquals(true, particle.isStopped());
		}
	}

	@DisplayName("A stopped particle...")
	@Nested
	class Stopped {
		@BeforeEach
		void before() {
			particle.stop();
		}

		@DisplayName("cannot be modified")
		@Test
		void modify() {
			assertThrows(IllegalStateException.class, () -> particle.add(null));
			assertThrows(NullPointerException.class, () -> particle.update());
		}

		@DisplayName("cannot be stopped more than once")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> particle.stop());
		}
	}
}
