package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;
import org.sarge.jove.volume.Bounds;
import org.sarge.lib.element.Element;

public class BoxPositionFactoryTest {
	private Randomiser randomiser;

	@BeforeEach
	void before() {
		randomiser = spy(Randomiser.class);
		when(randomiser.next()).thenReturn(1f);
	}

	@DisplayName("The box factory generates positions randomly within the given bounds")
	@Test
	void box() {
		final Point max = new Point(1, 2, 3);
		final var factory = new BoxPositionFactory(new Bounds(Point.ORIGIN, max), randomiser);
		assertEquals(max, factory.position());
	}

	@Test
	void load() {
		final String origin = "0 0 0";
		final Element e = new Element.Builder()
				.child("min", origin)
				.child("max", origin)
				.build();
		final PositionFactory factory = BoxPositionFactory.load(e, randomiser);
		assertNotNull(factory);
		assertEquals(Point.ORIGIN, factory.position());
	}
}
