package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Vector;

public class InfluenceTest {
	private Particle particle;

	@BeforeEach
	void before() {
		particle = mock(Particle.class);
	}

	@Test
	void ignore() {
		final Influence inf = spy(Influence.class);
		assertEquals(true, inf.ignoreStopped());
	}

	@Test
	void literal() {
		final Influence inf = Influence.of(Vector.Y);
		inf.apply(particle);
		verify(particle).add(Vector.Y);
	}
}
