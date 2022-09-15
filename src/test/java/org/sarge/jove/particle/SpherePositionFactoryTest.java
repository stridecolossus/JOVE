package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;
import org.sarge.lib.element.Element;

public class SpherePositionFactoryTest {
	private Randomiser randomiser;

	@BeforeEach
	void before() {
		randomiser = spy(Randomiser.class);
		when(randomiser.next()).thenReturn(1f);
	}

	@DisplayName("The sphere factory positions particles randomly on the surface of a sphere")
	@Test
	void sphere() {
		final var factory = new SpherePositionFactory(new Sphere(Point.ORIGIN, 3), randomiser);
		final Vector expected = new Vector(1, 1, 1).normalize().multiply(3);
		assertEquals(new Point(expected), factory.position());
	}

	@Test
	void load() {
		final Element e = new Element.Builder()
				.child("centre", "0 0 0")
				.child("radius", "1")
				.build();
		final PositionFactory factory = SpherePositionFactory.load(e, randomiser);
		assertNotNull(factory);
		assertEquals(new Point(new Vector(1, 1, 1).normalize()), factory.position());
	}
}
