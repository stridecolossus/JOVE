package org.sarge.jove.light;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.jove.geometry.Vector;

public class DirectionalLightTest extends AbstractLightTest<DirectionalLight> {
	@Override
	protected DirectionalLight createLight() {
		return new DirectionalLight();
	}

	@Override
	protected int getExpectedType() {
		return 2;
	}

	@Test
	public void setDirection() {
		final Vector dir = new Vector( 1, 2, 3 );
		light.setDirection( dir );
		assertEquals( dir, light.getDirection() );
	}
}
