package org.sarge.jove.particle;

import org.sarge.jove.common.Colour;
import org.sarge.jove.util.MathsUtil;

/**
 * Factory for particle colours.
 * @author Sarge
 */
@FunctionalInterface
public interface ColourFactory {
	/**
	 * @return Colour of next particle
	 */
	Colour getColour();

	/**
	 * @param col Colour
	 * @return Literal colour factory
	 */
	static ColourFactory colour(Colour col) {
		return () -> col;
	}

	/**
	 * Random colour factory.
	 */
	ColourFactory RANDOM = () -> new Colour(
		MathsUtil.RANDOM.nextFloat(),
		MathsUtil.RANDOM.nextFloat(),
		MathsUtil.RANDOM.nextFloat(),
		1
	);
}
