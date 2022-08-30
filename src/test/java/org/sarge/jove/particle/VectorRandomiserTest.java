package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Vector;

public class VectorRandomiserTest {
	private VectorRandomiser randomiser;

	@BeforeEach
	void before() {
		final Random random = new Random() {
			@Override
			public float nextFloat() {
				return 1;
			}
		};
		randomiser = new VectorRandomiser(random);
	}

	@Test
	void randomise() {
		assertEquals(new Vector(1, 1, 1), randomiser.randomise());
	}
}
