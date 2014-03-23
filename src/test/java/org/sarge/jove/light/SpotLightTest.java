package org.sarge.jove.light;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class SpotLightTest extends AbstractLightTest<SpotLight>{
	@Override
	protected SpotLight createLight() {
		return new SpotLight();
	}

	@Override
	protected int getExpectedType() {
		return 4;
	}

	@Test
	public void setPosition() {
		final Point pos = new Point( 1, 2, 3 );
		light.setPosition( pos );
		assertEquals( pos, light.getPosition() );
	}

	@Test
	public void setDirection() {
		final Vector dir = new Vector( 1, 2, 3 );
		light.setDirection( dir );
		assertEquals( dir, light.getDirection() );
	}
}
