package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

public class ParticleTest {
	private Particle particle;

	@BeforeEach
	void before() {
		particle = new Particle(1, Point.ORIGIN, Vector.Y);
	}

	@DisplayName("A new particle has an initial position and movement vector")
	@Test
	void constructor() {
		assertEquals(1, particle.time());
		assertEquals(Point.ORIGIN, particle.origin());
		assertEquals(Vector.Y, particle.direction());
		assertEquals(true, particle.isAlive());
		assertEquals(false, particle.isIdle());
	}

	@DisplayName("A particle can be moved")
	@Test
	void move() {
		particle.move(Vector.X);
		assertEquals(new Point(1, 0, 0), particle.origin());
	}

	@DisplayName("The movement vector of a particle can be combined")
	@Test
	void vector() {
		particle.add(Vector.X);
		assertEquals(Vector.Y.add(Vector.X), particle.direction());
	}

	@DisplayName("The velocity of a particle can be modified")
	@Test
	void velocity() {
		particle.velocity(2);
		assertEquals(Vector.Y.multiply(2), particle.direction());
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
		particle.reflect(pt, Vector.Y);
		assertEquals(pt, particle.origin());
		assertEquals(Vector.Y.invert(), particle.direction());
	}

	@DisplayName("A particle can be written to a vertex buffer")
	@Test
	void buffer() {
		final ByteBuffer bb = mock(ByteBuffer.class);
		when(bb.putFloat(0)).thenReturn(bb);
		particle.buffer(bb);
		verify(bb, times(3)).putFloat(0);
	}
}
