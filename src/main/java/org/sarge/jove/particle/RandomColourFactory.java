package org.sarge.jove.particle;

import org.sarge.jove.common.Colour;
import org.sarge.jove.util.MathsUtil;

/**
 * Factory for random colours.
 * @author Sarge
 */
public class RandomColourFactory implements ColourFactory {
	@Override
	public Colour getColour() {
		return new Colour(
			MathsUtil.RANDOM.nextFloat(),
			MathsUtil.RANDOM.nextFloat(),
			MathsUtil.RANDOM.nextFloat(),
			1
		);
	}
}
