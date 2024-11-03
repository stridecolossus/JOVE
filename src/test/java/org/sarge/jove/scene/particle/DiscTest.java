package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;

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
}
