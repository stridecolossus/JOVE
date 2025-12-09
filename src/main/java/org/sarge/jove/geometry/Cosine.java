package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtility;

/**
 * A <i>cosine</i> is a convenience record for the sine-cosine pair of a given angle.
 * @author Sarge
 */
public interface Cosine {
	/**
	 * @return Sine of the angle
	 */
	float sin();

	/**
	 * @return Cosine of the angle
	 */
	float cos();

	/**
	 * Convenience sine-cosine pair.
	 */
	record Pair(float sin, float cos) implements Cosine {
		@Override
		public final boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Cosine that) &&
					MathsUtility.isApproxEqual(this.sin, that.sin()) &&
					MathsUtility.isApproxEqual(this.cos, that.cos());
		}
	}

	/**
	 * A <i>cosine provider</i> calculates the sine-cosine pair for a given angle.
	 */
	interface Provider {
		/**
		 * Calculates the sin-cos pair for the given angle.
		 * @param angle Angle (radians)
		 * @return Cosine pair
		 */
    	Cosine cosine(float angle);

    	/**
    	 * Default implementation delegating to the built-in trigonometry functions.
    	 */
    	Provider DEFAULT = angle -> {
    		final float sin = (float) Math.sin(angle);
    		final float cos = (float) Math.cos(angle);
    		return new Pair(sin, cos);
    	};
    }
}

//	/**
//	 * @param quadrant Unit-circle quadrant (counter-clockwise).
//	 * @return Cosine of the given quadrant
//	 */
//	public static Cosine quadrant(int quadrant) {
//		return switch(quadrant & 3) {
//    		case 0 -> new Cosine(0, 1);
//    		case 1 -> new Cosine(1, 0);
//    		case 2 -> new Cosine(0, -1);
//    		case 3 -> new Cosine(-1, 0);
//    		default -> throw new RuntimeException();
//		};
//	}
//
//	/**
//	 *
//	 * i) small angle approximations
//	 *
//	 * for small angles (???) in radians:
//	 *
//	 * sin(a) ~ tan(a) ~ 0 OR a
//	 *
//	 * cos(a) ~ 1 - (a * a) / 2 ~ 1
//	 *
//	 * where 'small' is up to 0.5 radians
//	 *
//	 * ii)
//	 *
//	 * only store angles 0 .. PI/2, i.e. first quadrant
//	 *
//	 * reduces to 25% memory
//	 *
//	 * quadrant IV  -> sin(a) = -sin(-a)
//	 * quadrant II  -> sin(a) = sin(PI - a)
//	 * quadrant III -> sin(a) = -sina(PI + a)
//	 *
