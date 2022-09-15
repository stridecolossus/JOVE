package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.*;
import org.sarge.lib.element.Element;

public class ConeVectorFactoryTest {
	private VectorFactory factory;
	private Randomiser randomiser;

	@BeforeEach
	void before() {
		randomiser = mock(Randomiser.class);
		factory = new ConeVectorFactory(Axis.Y, MathsUtil.toRadians(45), randomiser);
	}

	@Test
	void zero() {
		when(randomiser.next()).thenReturn(0.5f);
		assertEquals(Axis.Y, factory.vector(null));
	}

	@Test
	void max() {
		final Vector x = new Vector(0, 1, 1);
		final Vector y = new Vector(-1, 1, 0);
		final Vector expected = x.add(y).normalize();
		when(randomiser.next()).thenReturn(1f);
		assertEquals(expected, factory.vector(null));
	}

	@Test
	void load() {
		final Element e = new Element.Builder()
				.child("normal", "0 1 0")
				.child("radius", "0")
				.build();
		final VectorFactory factory = ConeVectorFactory.load(e, randomiser);
		assertNotNull(factory);
		assertEquals(Axis.Y, factory.vector(null));
	}
}
