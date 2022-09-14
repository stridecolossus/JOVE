package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;

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
		// TODO
	}
}
