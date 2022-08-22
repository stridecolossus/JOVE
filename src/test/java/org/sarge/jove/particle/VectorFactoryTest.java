package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

class VectorFactoryTest {
	@DisplayName("The movement vector of a particle can be initialised to a literal vector")
	@Test
	void literal() {
		final VectorFactory factory = VectorFactory.of(Vector.X);
		assertEquals(Vector.X, factory.vector(null));
	}

	@DisplayName("The movement vector of a particle can be initialised to its position")
	@Test
	void position() {
		final Point pos = new Point(1, 2, 3);
		assertEquals(new Vector(pos), VectorFactory.POSITION.vector(pos));
	}

	@DisplayName("The random factory initialises particles movement vectors to a random direction")
	@Test
	void random() {
		final var factory = VectorFactory.random(new Random());
		assertNotNull(factory.vector(null));
	}
}
