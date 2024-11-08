package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Frame;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

class ParticleSystemTest {
	private ParticleSystem sys;
	private Frame frame;

	@BeforeEach
	void before() {
		frame = mock(Frame.class);
		when(frame.time()).thenReturn(Instant.ofEpochSecond(1));
		when(frame.elapsed()).thenReturn(Duration.ofSeconds(2));
		sys = new ParticleSystem();
	}

	// TODO - mesh tests

	/**
	 * Adds a particle to the system.
	 */
	private Particle create() {
		sys.add(1, 3);
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
			sys.update(frame);
			assertEquals(0, sys.size());
		}

		@DisplayName("can have new particles added")
		@Test
		void add() {
			sys.add(1, 0);
			assertEquals(1, sys.size());
			assertEquals(1, sys.particles().size());
		}

		@DisplayName("cannot add more particles than the configured maximum")
		@Test
		void max() {
			sys.max(1);
			sys.add(2, 0);
			assertEquals(1, sys.max());
			assertEquals(1, sys.size());
		}

		@DisplayName("can be configured to generate new particles on each frame")
		@Test
		void generate() {
			sys.policy(GenerationPolicy.fixed(1));
			sys.update(frame);
			assertEquals(1, sys.size());
		}

		@DisplayName("has a default position and direction for new particles")
		@Test
		void defaults() {
			final Particle p = create();
			assertEquals(Point.ORIGIN, p.ray().origin());
			assertEquals(Axis.Y, p.ray().direction());
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
			sys.vector(DirectionFactory.of(Axis.X));

			final Particle p = create();
			assertEquals(pos, p.ray().origin());
			assertEquals(Axis.X, p.ray().direction());
		}

		@DisplayName("has a creation timestamp")
		@Test
		void creation() {
			final Particle p = create();
			assertEquals(3, p.created());
		}

		@DisplayName("is moved by its current direction on each frame")
		@Test
		void update() {
			final Particle p = create();
			sys.update(frame);
			assertEquals(new Point(0, 2, 0), p.ray().origin());
		}

		@DisplayName("is modified by the configured influences on each frame")
		@Test
		void influence() {
			final Particle p = create();
			final Influence inf = mock(Influence.class);
			sys.add(inf);
			sys.update(frame);
			verify(inf).apply(p, 2f);
		}

		@DisplayName("is destroyed when its lifetime has expired")
		@Test
		void expired() {
			sys.lifetime(Duration.ofMillis(2));
			create();
			sys.update(frame);
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
			sys.update(frame);
			assertEquals(Point.ORIGIN, p.ray().origin());
		}

		@DisplayName("is not tested for collisions")
		@Test
		void collide() {
			final var surface = mock(Intersection.Surface.class);
			sys.add(surface, Collision.DESTROY);
			sys.update(frame);
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
			final var surface = mock(Intersection.Surface.class);
			sys.add(surface, Collision.DESTROY);
			sys.update(frame);
			assertEquals(false, p.isAlive());
		}

		@DisplayName("is culled")
		@Test
		void culled() {
			sys.update(frame);
			assertEquals(0, sys.size());
		}
	}
}
