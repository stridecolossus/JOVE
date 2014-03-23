package org.sarge.jove.material;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Test;
import org.sarge.jove.material.RenderProperty;
import org.sarge.jove.material.ToggleProperty;

public class TogglePropertyTest {
	@Test
	public void toggle() {
		// Create toggle
		final RenderProperty effect = mock( RenderProperty.class );
		final ToggleProperty toggle = new ToggleProperty( effect );
		assertEquals( true, toggle.isActive() );

		// Disable and check delegate effect is not applied
		toggle.setActive( false );
		toggle.apply( null );
		verifyZeroInteractions( effect );
		assertEquals( false, toggle.isActive() );

		// Enable and check now applied
		toggle.setActive( true );
		toggle.apply( null );
		verify( effect ).apply( null );
		assertEquals( true, toggle.isActive() );
	}
}
