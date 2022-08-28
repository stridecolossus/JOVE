package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Animator;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersects;
import org.sarge.jove.particle.ParticleSystem.*;

class ParticleSystemTest {
	private ParticleSystem sys;
	private Animator animator;

	@BeforeEach
	void before() {
		sys = new ParticleSystem();
		animator = mock(Animator.class);
	}

	/**
	 * Adds a particle to the system.
	 */
	private Particle create() {
		sys.add(1);
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
			sys.add(1);
			assertEquals(1, sys.size());
		}

		@DisplayName("can be configured to generate new particles on each frame")
		@Test
		void generate() {
			sys.policy(Policy.increment(1));
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
			verify(inf).apply(p, 0);
		}
	}

	@DisplayName("A particle that intersects a collision surface...")
	@Nested
	class Collisions {
		private Intersects surface;
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
		}

		@DisplayName("can be reflected by the surface")
		@Test
		void reflect() {
			sys.add(surface, CollisionAction.REFLECT);
			sys.update(animator);
			assertEquals(Point.ORIGIN, p.origin());
			assertEquals(Vector.Y.invert(), p.direction());
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
			final Intersects surface = mock(Intersects.class);
			sys.add(surface, CollisionAction.DESTROY);
			sys.update(animator);
			verifyNoInteractions(surface);
		}
	}

	@Nested
	class PolicyTests {
		@DisplayName("The NONE policy does not generate particles")
		@Test
		void none() {
			assertEquals(0, Policy.NONE.count(0));
		}

		@DisplayName("An incremental growth policy adds a constant number of particles")
		@Test
		void increment() {
			final Policy policy = Policy.increment(1);
			assertEquals(1, policy.count(0));
			assertEquals(1, policy.count(1));
		}

		@DisplayName("A maximum growth policy caps the number of particles")
		@Test
		void max() {
			final Policy policy = Policy.increment(3).max(2);
			assertEquals(2, policy.count(0));
		}
	}
}
