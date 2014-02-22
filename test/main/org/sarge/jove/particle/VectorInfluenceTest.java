package org.sarge.jove.particle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.sarge.jove.geometry.Vector;

public class VectorInfluenceTest {
	@Test
	public void apply() {
		final Particle p = mock( Particle.class );
		final VectorInfluence inf = new VectorInfluence( new Vector( 0.2f, 0.4f, 0.6f ) );
		inf.apply( p, 500L );
		verify( p ).add( new Vector( 0.1f, 0.2f, 0.3f ) );
	}
}
