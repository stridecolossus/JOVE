package org.sarge.jove.particle;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.lib.io.ClasspathDataSource;

public class ParticleSystemLoaderTest {
	@Test
	public void load() throws Exception {
		// Load particle system
		final ParticleSystemLoader loader = new ParticleSystemLoader( new ClasspathDataSource( ParticleSystemLoaderTest.class ) );
		/*final ParticleSystem sys = */ loader.load( "particle.system.xml", mock( RenderingSystem.class ) );
//		assertNotNull( sys );
//
//		// Verify particle system
//		assertEquals( true, sys.getParticles().isEmpty() );
	}
}
