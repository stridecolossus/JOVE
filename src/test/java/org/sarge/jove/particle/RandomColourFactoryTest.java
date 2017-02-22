package org.sarge.jove.particle;

import static org.junit.Assert.assertNotNull;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Test;
import org.sarge.jove.common.Colour;

public class RandomColourFactoryTest {
	@Test
	public void getColour() {
		final Colour col = ColourFactory.RANDOM.getColour();
		assertNotNull(col);
		assertFloatEquals(1, col.a);
	}
}
