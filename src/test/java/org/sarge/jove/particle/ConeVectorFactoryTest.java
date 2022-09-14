package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.*;

public class ConeVectorFactoryTest {
	private VectorFactory factory;
	private Randomiser random;

	@BeforeEach
	void before() {
		random = mock(Randomiser.class);
		factory = new ConeVectorFactory(Axis.Y, MathsUtil.toRadians(45), random);
	}

	@Test
	void zero() {
		when(random.next()).thenReturn(0.5f);
		assertEquals(Axis.Y, factory.vector(null));
	}

	@Test
	void max() {
		final Vector x = new Vector(0, 1, 1);
		final Vector y = new Vector(-1, 1, 0);
		final Vector expected = x.add(y).normalize();
		when(random.next()).thenReturn(1f);
		assertEquals(expected, factory.vector(null));
	}

	@Test
	void load() {
		// TODO
	}
}
