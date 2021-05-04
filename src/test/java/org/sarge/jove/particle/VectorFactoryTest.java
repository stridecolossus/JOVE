package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class VectorFactoryTest {
	@Test
	public void position() {
		final Point pos = new Point(1, 2, 3);
		assertEquals(new Vector(pos), VectorFactory.POSITION.vector(pos));
	}

	@Test
	public void literal() {
		final VectorFactory factory = VectorFactory.literal(Vector.X);
		assertEquals(Vector.X, factory.vector(null));
	}

	@Test
	public void random() {
		final var factory = VectorFactory.random(new Random());
		assertNotNull(factory);
		assertNotNull(factory.vector(null));
	}

	@Test
	public void scaled() {
		final VectorFactory factory = VectorFactory.literal(Vector.X);
		final VectorFactory scaled = VectorFactory.scaled(factory, 3);
		assertEquals(new Vector(3, 0, 0), scaled.vector(null));
	}
}
