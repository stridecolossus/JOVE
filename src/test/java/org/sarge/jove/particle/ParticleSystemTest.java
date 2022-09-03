package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersected;
import org.sarge.jove.particle.ParticleSystem.CollisionAction;

class ParticleSystemTest {
	private ParticleSystem sys;
	private Animator animator;

	@BeforeEach
	void before() {
		sys = new ParticleSystem();
		animator = mock(Animator.class);
		when(animator.elapsed()).thenReturn(1000L);
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
			sys.policy(GrowthPolicy.increment(1));
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

	@DisplayName("A particle that intersects a collision surface...")
	@Nested
	class Collisions {
		private Intersected surface;
		private Particle p;

		@BeforeEach
		void before() {
			p = create();
			surface = new Plane(Vector.Y, -1);
		}

		@DisplayName("can be destroyed")
		@Test
		void destroy() {
			sys.add(surface, CollisionAction.DESTROY);
			sys.update(animator);
			assertEquals(0, sys.size());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			sys.add(surface, CollisionAction.STOP);
			sys.update(animator);
			assertEquals(true, p.isIdle());
			assertEquals(List.of(p), sys.particles());
		}

		@DisplayName("can be reflected by the surface")
		@Test
		void reflect() {
			sys.add(surface, CollisionAction.REFLECT);
			sys.update(animator);
			assertEquals(new Point(0, 1, 0), p.origin());
			assertEquals(Vector.Y.invert(), p.direction());
			assertEquals(List.of(p), sys.particles());
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
		void move() {
			sys.update(animator);
			assertEquals(Point.ORIGIN, p.origin());
		}

		@DisplayName("is not tested for collisions")
		@Test
		void collide() {
			final Intersected surface = mock(Intersected.class);
			sys.add(surface, CollisionAction.DESTROY);
			sys.update(animator);
			verifyNoInteractions(surface);
		}
	}
}
