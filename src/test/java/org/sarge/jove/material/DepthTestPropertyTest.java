package org.sarge.jove.material;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;

public class DepthTestPropertyTest {
	@Test
	public void apply() {
		// Set depth-test function
		final RenderingSystem sys = mock( RenderingSystem.class );
		final DepthTestProperty effect = new DepthTestProperty( ">" );
		effect.apply( sys );
		verify( sys ).setDepthTest( effect );

		// Restore default
		effect.reset( sys );
		verify( sys ).setDepthTest( DepthTestProperty.DEFAULT );
	}
}
