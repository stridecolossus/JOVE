package org.sarge.jove.material;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.FaceCulling;

public class FaceCullPropertyTest {
	@Test
	public void apply() {
		// Cull back faces
		final RenderingSystem sys = mock( RenderingSystem.class );
		final FaceCullProperty effect = new FaceCullProperty( FaceCulling.FRONT );
		effect.apply( sys );
		verify( sys ).setFaceCulling( FaceCulling.FRONT );

		// Restore default
		effect.reset( sys );
		verify( sys ).setFaceCulling( FaceCulling.BACK );
	}
}
