package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;

class PositionFactoryTest {
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
		assertNotNull(factory);
		assertEquals(pt, factory.position());
	}

	@DisplayName("The circle factory generates positions on a disc about an origin point")
	@Test
	void disc() {
		final Randomiser randomiser = mock(Randomiser.class);
		when(randomiser.next()).thenReturn(0.5f);
		final Disc disc = new Disc(Axis.Y.vector(), 0, randomiser);
		final PositionFactory circle = PositionFactory.circle(Point.ORIGIN, disc);
		assertNotNull(circle);
		assertEquals(Point.ORIGIN, circle.position());
	}
}
