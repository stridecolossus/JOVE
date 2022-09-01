package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.*;

public class ConeVectorFactoryTest {
	private VectorFactory factory;
	private FloatSupplier random;

	@BeforeEach
	void before() {
		random = mock(FloatSupplier.class);
		factory = new ConeVectorFactory(Vector.Y, MathsUtil.HALF_PI, random);
	}

	@Test
	void zero() {
		when(random.get()).thenReturn(0f);
		assertEquals(Vector.Y, factory.vector(null));
	}

	@Test
	void one() {
		final Vector expected = new Vector(1, 0, -1).normalize();
		when(random.get()).thenReturn(1f);
		assertEquals(expected, factory.vector(null));
	}
}
