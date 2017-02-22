package org.sarge.jove.particle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Vector;

public class InfluenceTest {
	private Particle particle;
	
	@Before
	public void before() {
		particle = mock(Particle.class);
	}
	
	@Test
	public void vector() {
		final Influence influence = Influence.vector(Vector.X_AXIS);
		influence.apply(particle, 1000L);
		verify(particle).add(Vector.X_AXIS);
	}
	
	@Test
	public void velocity() {
		when(particle.getDirection()).thenReturn(Vector.X_AXIS);
		final Influence influence = Influence.velocity(2f);
		influence.apply(particle, 1000L);
		verify(particle).setDirection(Vector.X_AXIS.multiply(2));
	}
	
	@Test
	public void fade() {
		when(particle.getAlpha()).thenReturn(0.5f);
		final Influence influence = Influence.fade(0.5f);
		influence.apply(particle, 1000L);
		verify(particle).setAlpha(0.25f);
	}
}
