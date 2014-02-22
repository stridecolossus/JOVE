package org.sarge.jove.particle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.sarge.jove.common.Colour;

public class RandomColourFactoryTest {
	@Test
	public void getColour() {
		final ColourFactory factory = new RandomColourFactory();
		final Colour col = factory.getColour();
		assertNotNull( col );
		assertEquals( 1, col.getAlpha(), 0.0001f );
	}
}
