package org.sarge.jove.geometry;

import static org.sarge.jove.util.MathsUtility.isApproxEqual;

import org.sarge.jove.util.FloatSupport.FloatUnaryOperator;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>cosine</i> is a convenience record for the sine-cosine pair of a given angle.
 * @author Sarge
 */
public record Cosine(float sin, float cos) {
	/**
	 * @param quadrant Unit-circle quadrant (counter-clockwise).
	 * @return Cosine of the given quadrant
	 */
	public static Cosine quadrant(int quadrant) {
		return switch(quadrant & 3) {
    		case 0 -> new Cosine(0, 1);
    		case 1 -> new Cosine(1, 0);
    		case 2 -> new Cosine(0, -1);
    		case 3 -> new Cosine(-1, 0);
    		default -> throw new RuntimeException();
		};
	}

	@Override
	public final boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Cosine that) &&
				isApproxEqual(this.sin, that.sin) &&
				isApproxEqual(this.cos, that.cos);
	}

	@Override
	public final String toString() {
		return "Cosine" + MathsUtility.format(sin, cos);
	}

	/**
	 * A <i>cosine provider</i> is a factory for the sine-cosine pair of a given angle.
	 */
	@FunctionalInterface
	public interface Provider {
		/**
		 * @param angle Angle (radians)
		 * @return Cosine of the given angle
		 */
		Cosine cosine(float angle);

    	/**
    	 * Default implementation that delegates to the JDK maths library.
    	 */
    	Provider DEFAULT = of(angle -> (float) Math.cos(angle));

    	/**
    	 * Creates a provider as an adapter for the given cosine function.
    	 * TODO
    	 * @param function Cosine function
    	 * @return Cosine provider
    	 */
    	static Provider of(FloatUnaryOperator function) {
    		return angle -> {
    			final float sin = (float) Math.sin(angle); // function.apply(angle - MathsUtility.HALF_PI);
    			final float cos = (float) Math.cos(angle); // function.apply(angle);
    			return new Cosine(sin, cos);
    		};
    	}
    }
}
