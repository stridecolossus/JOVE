package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Axis;
import org.sarge.jove.scene.particle.*;
import org.sarge.jove.util.Randomiser;

class VectorFactoryTest {
	private Randomiser randomiser;

	@BeforeEach
	void before() {
		randomiser = mock(Randomiser.class);
	}

	@DisplayName("The movement vector of a particle can be initialised to a literal vector")
	@Test
	void literal() {
		final VectorFactory factory = VectorFactory.of(Axis.X);
		assertEquals(Axis.X, factory.vector());
	}

	@DisplayName("The random factory initialises particles movement vectors to a random direction")
	@Test
	void random() {
		final var factory = VectorFactory.random(randomiser);
		when(randomiser.vector()).thenReturn(Axis.Y);
		assertNotNull(factory);
		assertEquals(Axis.Y, factory.vector());
	}

	@DisplayName("The cone factory generates randomised vectors specified by a disc")
	@Test
	void disc() {
		final Disc disc = new Disc(Axis.Y, 0, randomiser);
		final var cone = VectorFactory.cone(disc);
		when(randomiser.next()).thenReturn(0.5f);
		assertNotNull(cone);
		assertEquals(Axis.Y, cone.vector());
	}
}
