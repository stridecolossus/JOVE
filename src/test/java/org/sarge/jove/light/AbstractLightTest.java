package org.sarge.jove.light;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Colour;

public abstract class AbstractLightTest<T extends Light> {
	protected T light;

	@Before
	public void before() {
		light = createLight();
	}

	protected abstract T createLight();

	protected abstract int getExpectedType();

	@Test
	public void constructor() {
		assertEquals( Colour.WHITE, light.getColour() );
		assertEquals( getExpectedType(), light.getType() );
	}
}
