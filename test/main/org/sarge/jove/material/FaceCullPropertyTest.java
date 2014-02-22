package org.sarge.jove.material;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.Face;

public class FaceCullPropertyTest {
	@Test
	public void apply() {
		// Cull back faces
		final RenderingSystem sys = mock( RenderingSystem.class );
		final FaceCullProperty effect = new FaceCullProperty( Face.FRONT );
		effect.apply( sys );
		verify( sys ).setFaceCulling( Face.FRONT );

		// Restore default
		effect.reset( sys );
		verify( sys ).setFaceCulling( Face.BACK );
	}
}
