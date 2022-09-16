package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.*;
import org.sarge.lib.element.Element;

public class DiscTest {
	private Disc disc;
	private Randomiser randomiser;

	@BeforeEach
	void before() {
		randomiser = mock(Randomiser.class);
		disc = new Disc(Axis.Y, 1, randomiser);
	}

	@Test
	void origin() {
		when(randomiser.next()).thenReturn(0.5f);
		assertEquals(Point.ORIGIN, disc.point());
		assertEquals(Axis.Y, disc.vector());
	}

	@Test
	void radius() {
		final Vector x = new Vector(0, 1, 1);
		final Vector y = new Vector(-1, 1, 0);
		final Vector expected = x.add(y).normalize();

		// TODO - this feels nasty
		disc = new Disc(Axis.Y, MathsUtil.toRadians(45), randomiser);

		when(randomiser.next()).thenReturn(1f);
		//assertEquals(new Point(1, 0, 1), disc.point());

		assertEquals(expected, disc.vector());
	}

	@Test
	void load() {
		final Element e = new Element.Builder()
				.child("normal", "0 1 0")
				.child("radius", "0")
				.build();

		disc = Disc.load(e, randomiser);
		assertNotNull(disc);

		when(randomiser.next()).thenReturn(0.5f);
		assertEquals(Axis.Y, disc.vector());
	}
}
