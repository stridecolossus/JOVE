package org.sarge.jove.geometry;

/**
 * A <i>winding order</i> defines the <i>orientation</i> of a triangle or polygon with respect to the viewer.
 * @author Sarge
 */
public enum WindingOrder {
	CLOCKWISE,
	COUNTER_CLOCKWISE,
	COLINEAR;

	/**
	 * Determines the winding order for the given <i>determinant</i> of a polygon.
	 * @param det Determinant
	 * @return Winding order
	 */
	public static WindingOrder of(float det) {
		if(det > 0) {
			return COUNTER_CLOCKWISE;
		}
		else
		if(det < 0) {
			return CLOCKWISE;
		}
		else {
			return COLINEAR;
		}
	}
}
