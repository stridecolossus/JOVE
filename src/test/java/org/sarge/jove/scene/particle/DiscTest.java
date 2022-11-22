package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.scene.particle.Disc;
import org.sarge.jove.util.Randomiser;
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
		when(randomiser.next()).thenReturn(1f);
		assertEquals(new Point(1, 0, 1), disc.point());
		assertEquals(new Vector(1, 1, 1).normalize(), disc.vector());
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
