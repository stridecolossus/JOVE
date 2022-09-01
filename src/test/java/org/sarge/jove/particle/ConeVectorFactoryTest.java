package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

public class ConeVectorFactoryTest {
	private VectorFactory factory;
	private Random random;
	private float angle;

	@BeforeEach
	void before() {
		// TODO - random -> float supplier, easier to mock
		random = new Random() {
			@Override
			public float nextFloat(float bound) {
				return angle;
			}
		};
		factory = new ConeVectorFactory(Vector.Y, MathsUtil.HALF_PI, random);
	}

	@Test
	void vector() {
		assertEquals(Vector.Z.invert(), factory.vector(null));
	}
}
