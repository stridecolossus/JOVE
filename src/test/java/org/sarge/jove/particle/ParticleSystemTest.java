package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersected;
import org.sarge.jove.particle.ParticleSystem.Characteristic;

class ParticleSystemTest {
	private ParticleSystem sys;
	private Animator animator;

	@BeforeEach
	void before() {
		sys = new ParticleSystem(Characteristic.CULL);
		animator = mock(Animator.class);
		when(animator.position()).thenReturn(1f);
	}

	/**
	 * Adds a particle to the system.
	 */
	private Particle create() {
		sys.add(1, animator.time());
		return sys.particles().iterator().next();
	}

	@DisplayName("A new particle system...")
	class New {
		@DisplayName("does not contain any particles")
		@Test
		void empty() {
			assertEquals(0, sys.size());
			assertEquals(List.of(), sys.particles());
		}

		@DisplayName("does nothing on a frame update")
		@Test
		void update() {
			sys.update(animator);
			assertEquals(0, sys.size());
		}

		@DisplayName("can have new particles added")
		@Test
		void add() {
			sys.add(1, 0);
			assertEquals(1, sys.size());
			assertEquals(1, sys.particles().size());
		}

		@DisplayName("can be configured to generate new particles on each frame")
		@Test
		void generate() {
			sys.policy(GenerationPolicy.fixed(1));
			sys.update(animator);
			assertEquals(1, sys.size());
		}

		@DisplayName("has a default position and direction for new particles")
		@Test
		void defaults() {
			final Particle p = create();
			assertEquals(Point.ORIGIN, p.origin());
			assertEquals(Vector.Y, p.direction());
		}
	}

	@DisplayName("A particle in a system...")
	@Nested
	class ParticleTests {
		@DisplayName("has an initial position and direction according to the configured factories")
		@Test
		void position() {
			final Point pos = new Point(1, 2, 3);
			sys.position(PositionFactory.of(pos));
			sys.vector(VectorFactory.of(Vector.X));

			final Particle p = create();
			assertEquals(pos, p.origin());
			assertEquals(Vector.X, p.direction());
			assertEquals(0, p.time());
		}

		@DisplayName("has a creation timestamp")
		@Test
		void creation() {
			when(animator.time()).thenReturn(3L);
			final Particle p = create();
			assertEquals(3L, p.time());
		}

		@DisplayName("is moved by its current direction on each frame")
		@Test
		void update() {
			final Particle p = create();
			sys.update(animator);
			assertEquals(new Point(Vector.Y), p.origin());
		}

		@DisplayName("is modified by the configured influences on each frame")
		@Test
		void influence() {
			final Particle p = create();
			final Influence inf = mock(Influence.class);
			sys.add(inf);
			sys.update(animator);
			verify(inf).apply(p, 1f);
		}

		@DisplayName("is destroyed when its lifetime has expired")
		@Test
		void expired() {
			sys.lifetime(2L);
			create();
			when(animator.time()).thenReturn(3L);
			sys.update(animator);
			assertEquals(0, sys.size());
		}
	}

	@DisplayName("A stopped particle...")
	@Nested
	class Stopped {
		private Particle p;

		@BeforeEach
		void before() {
			p = create();
			p.stop();
		}

		@DisplayName("is not updated by the particle system")
		@Test
		void update() {
			sys.update(animator);
			assertEquals(Point.ORIGIN, p.origin());
		}

		@DisplayName("is not tested for collisions")
		@Test
		void collide() {
			final Intersected surface = mock(Intersected.class);
			sys.add(surface, Collision.DESTROY);
			sys.update(animator);
			verifyNoInteractions(surface);
		}
	}

	@DisplayName("A destroyed particle...")
	@Nested
	class Destroyed {
		private Particle p;

		@BeforeEach
		void before() {
			p = create();
			p.destroy();
		}

		@DisplayName("is not updated by the particle system")
		@Test
		void update() {
			final Intersected surface = mock(Intersected.class);
			sys.add(surface, Collision.DESTROY);
			sys.update(animator);
			assertEquals(false, p.isAlive());
		}

		@DisplayName("is culled")
		@Test
		void culled() {
			sys.update(animator);
			assertEquals(0, sys.size());
		}
	}
}
