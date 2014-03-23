package org.sarge.jove.light;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.jove.geometry.Point;

public class PointLightTest extends AbstractLightTest<PointLight> {
	@Override
	protected PointLight createLight() {
		return new PointLight();
	}

	@Override
	protected int getExpectedType() {
		return 3;
	}

	@Test
	public void setPosition() {
		final Point pos = new Point( 1, 2, 3 );
		light.setPosition( pos );
		assertEquals( pos, light.getPosition() );
	}
}
