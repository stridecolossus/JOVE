package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

public class ParticleTest {
	private Particle particle;

	@BeforeEach
	void before() {
		particle = new Particle(Point.ORIGIN, Vector.Y);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, particle.origin());
		assertEquals(Vector.Y, particle.direction());
		assertEquals(false, particle.isIdle());
	}

	@Test
	void move() {
		particle.move(Vector.X);
		assertEquals(new Point(1, 0, 0), particle.origin());
	}

	@Test
	void vector() {
		particle.add(Vector.X);
		assertEquals(Vector.Y.add(Vector.X), particle.direction());
	}

	@Test
	void velocity() {
		particle.velocity(2);
		assertEquals(Vector.Y.multiply(2), particle.direction());
	}

	@Test
	void update() {
		particle.update();
		assertEquals(new Point(Vector.Y), particle.origin());
	}

	@Test
	void stop() {
		particle.stop();
		assertEquals(true, particle.isIdle());
	}

	@Test
	void stopped() {
		particle.stop();
		assertThrows(IllegalStateException.class, () -> particle.stop());
	}

	@Test
	void reflect() {
		final Intersection intersection = Intersection.of(0, Vector.Y);
		particle.update();
		particle.reflect(intersection);
		assertEquals(Point.ORIGIN, particle.origin());
		assertEquals(Vector.Y.invert(), particle.direction());
	}

	@Test
	void buffer() {
		final ByteBuffer bb = mock(ByteBuffer.class);
		when(bb.putFloat(0)).thenReturn(bb);
		particle.buffer(bb);
		verify(bb, times(3)).putFloat(0);
	}
}
