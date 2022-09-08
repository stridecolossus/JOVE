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
		randomiser = mock(Randomiser.class);
		when(randomiser.vector()).thenReturn(Axis.X);
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
		final var factory = PositionFactory.box(new Bounds(Point.ORIGIN, new Point(1, 2, 3)), randomiser);
		assertEquals(new Point(1, 0, 0), factory.position());
	}

	@DisplayName("The sphere factory positions particles randomly on the surface of a sphere")
	@Test
	void spherical() {
		final var vol = new SphereVolume(Point.ORIGIN, 3);
		final var factory = PositionFactory.sphere(vol, randomiser);
		assertEquals(new Point(3, 0, 0), factory.position());
	}
}
