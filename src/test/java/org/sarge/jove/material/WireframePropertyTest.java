package org.sarge.jove.material;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;

public class WireframePropertyTest {
	private WireframeProperty effect;
	private RenderingSystem sys;

	@Before
	public void before() {
		effect = new WireframeProperty();
		sys = mock( RenderingSystem.class );
	}

	@Test
	public void apply() {
		// Apply
		effect.apply( sys );
		verify( sys ).setWireframeMode( true );

		// Remove
		effect.reset( sys );
		verify( sys ).setWireframeMode( false );
	}
}
