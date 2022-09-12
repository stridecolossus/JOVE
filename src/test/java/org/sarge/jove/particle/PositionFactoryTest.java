package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;

class PositionFactoryTest {
	private Randomiser randomiser;

	@BeforeEach
	void before() {
		randomiser = spy(Randomiser.class);
		when(randomiser.next()).thenReturn(1f);
	}

	@DisplayName("The origin factory positions particles at the origin")
	@Test
	void origin() {
		assertEquals(Point.ORIGIN, PositionFactory.ORIGIN.position());
	}

	@DisplayName("The literal factory positions particles at the given point")
	@Test
	void point() {
		final Point pt = new Point(1, 2, 3);
		final PositionFactory factory = PositionFactory.of(pt);
		assertEquals(pt, factory.position());
	}

	@DisplayName("The box factory positions particles randomly within the given bounds")
	@Test
	void box() {
		final Point max = new Point(1, 2, 3);
		final var factory = PositionFactory.box(new Bounds(Point.ORIGIN, max), randomiser);
		assertEquals(max, factory.position());
	}

	@DisplayName("The sphere factory positions particles randomly on the surface of a sphere")
	@Test
	void spherical() {
		final var factory = PositionFactory.sphere(new Sphere(Point.ORIGIN, 3), randomiser);
		final Vector expected = new Vector(1, 1, 1).normalize().multiply(3);
		assertEquals(new Point(expected), factory.position());
	}
}
