package org.sarge.jove.particle;

import org.sarge.jove.common.Colour;
import org.sarge.jove.util.RandomUtil;

/**
 * Factory for random colours.
 * @author Sarge
 */
public class RandomColourFactory implements ColourFactory {
	@Override
	public Colour getColour() {
		return new Colour(
			RandomUtil.RANDOM.nextFloat(),
			RandomUtil.RANDOM.nextFloat(),
			RandomUtil.RANDOM.nextFloat(),
			1
		);
	}
}
