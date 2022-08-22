package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.particle.CollisionSurface.Action;
import org.sarge.jove.particle.ParticleSystem.Policy;

class ParticleSystemTest {
	private ParticleSystem sys;
	private Particle particle;

	@BeforeEach
	void before() {
		sys = new ParticleSystem() {
			@Override
			protected Particle particle(Point pos, Vector vec) {
				particle = super.particle(pos, vec);
				return particle;
			}
		};
	}

	@DisplayName("An empty particle system...")
	class Empty {
		@DisplayName("does not contain any particles")
		@Test
		void empty() {
			assertEquals(0, sys.particles().count());
		}

		@DisplayName("does nothing on a frame update")
		@Test
		void update() {
			sys.completed(null);
			assertEquals(0, sys.particles().count());
		}

		@DisplayName("can generate new particles")
		@Test
		void add() {
			sys.add(1);
			assertNotNull(particle);
			assertEquals(1, sys.particles().count());
			assertEquals(Point.ORIGIN, particle.position());
		}

		@DisplayName("can initialise the position of new particles")
		@Test
		void position() {
			final Point pos = new Point(1, 2, 3);
			sys.position(PositionFactory.of(pos));
			sys.add(1);
			assertEquals(pos, particle.position());
		}

		@DisplayName("can initialise the initial movement vector of new particles")
		@Test
		void vector() {
			sys.vector(VectorFactory.of(Vector.X));
			sys.add(1);
			assertEquals(new Point(1, 0, 0), particle.vector());
		}
	}

	@DisplayName("On each frame a particle system...")
	@Nested
	class Updated {
		@BeforeEach
		void before() {
			sys.add(1);
		}

		@DisplayName("moves each particle by its current vector")
		@Test
		void move() {
			sys.completed(null);
			assertEquals(new Point(Vector.Y), particle.position());
		}

		@DisplayName("applies influences to each particle")
		@Test
		void influence() {
			final Influence inf = mock(Influence.class);
			sys.add(inf);
			sys.completed(null);
			verify(inf).apply(particle);
		}

		@DisplayName("checks for particle collisions")
		@Test
		void collision() {
			final CollisionSurface surface = mock(CollisionSurface.class);
			sys.add(surface, Action.DESTROY);
			sys.completed(null);
			verify(surface).intersects(particle.position());
		}

		@DisplayName("generates new particles according to the configured policy")
		@Test
		void generate() {
			sys.policy(Policy.increment(1));
			sys.completed(null);
			assertEquals(2, sys.particles().count());
		}
	}

	@DisplayName("A particle that intersects a collision surface...")
	@Nested
	class Collisions {
		private CollisionSurface surface;

		@BeforeEach
		void before() {
			sys.add(1);
			surface = mock(CollisionSurface.class);
			when(surface.intersects(any())).thenReturn(true);
		}

		@DisplayName("can be destroyed")
		@Test
		void destroy() {
			sys.add(surface, Action.DESTROY);
			sys.completed(null);
			assertEquals(0, sys.particles().count());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			sys.add(surface, Action.STOP);
			sys.completed(null);
			assertEquals(true, particle.isStopped());
		}

		@DisplayName("can be reflected by the surface")
		@Test
		void reflect() {
			sys.add(surface, Action.REFLECT);
			sys.completed(null);
			// TODO
		}
	}

	@DisplayName("A stopped particle...")
	@Nested
	class Stopped {
		@BeforeEach
		void before() {
			sys.add(1);
			particle.stop();
		}

		@DisplayName("is not updated by the particle system")
		@Test
		void move() {
			sys.completed(null);
			assertEquals(Point.ORIGIN, particle.position());
		}

		@DisplayName("is not affected by influences by default")
		@Test
		void stopped() {
			final Influence inf = spy(Influence.class);
			sys.add(inf);
			sys.completed(null);
			verify(inf, never()).apply(particle);
		}

		@DisplayName("is not tested for collisions")
		@Test
		void collide() {
			final CollisionSurface surface = mock(CollisionSurface.class);
			sys.add(surface, Action.DESTROY);
			sys.completed(null);
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
