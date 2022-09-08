package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;

/**
 * A <i>normalized vector</i> is a unit vector with a magnitude of <b>one</b>.
 * @author Sarge
 */
public class NormalizedVector extends Vector {
	/**
	 * @param len Vector length
	 * @return Whether the given vector length is normalised
	 */
	protected static boolean isNormalized(float len) {
		return MathsUtil.isEqual(1, len);
	}

	/**
	 * Constructor.
	 * @param vec Normalized vector
	 */
	NormalizedVector(Vector vec) {
		super(vec);
		assert isNormalized(vec.magnitude());
	}

	@Override
	public float magnitude() {
		return 1;
	}

	@Override
	public NormalizedVector normalize() {
		return this;
	}
}
