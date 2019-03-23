package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class ParticleSystemTest {
	private ParticleSystem sys;

	@BeforeEach
	public void before() {
		sys = new ParticleSystem.Builder().build();
	}

	@Test
	public void constructor() {
		assertNotNull(sys.particles());
		assertEquals(0, sys.particles().count());
	}

	@Test
	public void add() {
		sys.add();
		assertEquals(1, sys.particles().count());
	}

	@Test
	public void update() {

	}

	@Nested
	class BuilderTest {
		private ParticleSystem.Builder builder;
		private Particle particle;

		@BeforeEach
		public void before() {
			builder = new ParticleSystem.Builder();
			particle = null;
		}

		private void create() {
			sys = builder.build();
			sys.add();
			assertEquals(1, sys.particles().count());
			particle = sys.particles().iterator().next();
			assertNotNull(particle);
		}

		@Test
		public void position() {
			final Point pos = new Point(1, 2, 3);
			builder.position(PositionFactory.of(pos));
			create();
			assertEquals(pos, particle.position());
		}

		@Test
		public void vector() {
			builder.vector(VectorFactory.literal(Vector.X_AXIS));
			create();
			assertEquals(Vector.X_AXIS, particle.vector());
		}
	}
}
