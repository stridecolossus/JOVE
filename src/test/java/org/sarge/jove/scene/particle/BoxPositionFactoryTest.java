package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.scene.volume.Bounds;
import org.sarge.jove.util.Randomiser;

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
}
