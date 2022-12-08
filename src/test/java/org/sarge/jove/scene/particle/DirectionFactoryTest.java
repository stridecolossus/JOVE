package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Axis;
import org.sarge.jove.util.Randomiser;

class DirectionFactoryTest {
	private Randomiser randomiser;

	@BeforeEach
	void before() {
		randomiser = mock(Randomiser.class);
	}

	@DisplayName("The movement vector of a particle can be initialised to a literal vector")
	@Test
	void literal() {
		final DirectionFactory factory = DirectionFactory.of(Axis.X);
		assertEquals(Axis.X, factory.direction());
	}

	@DisplayName("The random factory initialises particles movement vectors to a random direction")
	@Test
	void random() {
		final var factory = DirectionFactory.random(randomiser);
		when(randomiser.vector()).thenReturn(Axis.Y);
		assertNotNull(factory);
		assertEquals(Axis.Y, factory.direction());
	}

	@DisplayName("The cone factory generates randomised vectors specified by a disc")
	@Test
	void disc() {
		final Disc disc = new Disc(Axis.Y, 0, randomiser);
		final var cone = DirectionFactory.cone(disc);
		when(randomiser.next()).thenReturn(0.5f);
		assertNotNull(cone);
		assertEquals(Axis.Y, cone.direction());
	}
}
