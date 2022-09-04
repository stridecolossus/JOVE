package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Vector;

public class RandomiserTest {
	private Randomiser randomiser;
	private Random random;

	@BeforeEach
	void before() {
		random = new Random() {
			@Override
			public float nextFloat() {
				return 0;
			}
		};
		randomiser = new Randomiser(random);
	}

	@Test
	void constructor() {
		assertEquals(random, randomiser.random());
	}

	@Test
	void next() {
		assertEquals(0, randomiser.next());
	}

	@Test
	void vector() {
		assertEquals(new Vector(-1, -1, -1), randomiser.vector());
	}
}
