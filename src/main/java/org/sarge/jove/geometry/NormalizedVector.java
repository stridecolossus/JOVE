package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;

/**
 * A <i>normalized vector</i> is a unit vector (magnitude of <b>one</b>).
 * @author Sarge
 */
public class NormalizedVector extends Vector {
	/**
	 * Constructor.
	 * @param vec Normalized vector
	 */
	NormalizedVector(Vector vec) {
		super(vec);
		assert MathsUtil.isEqual(1, vec.magnitude());
	}

	@Override
	public final float magnitude() {
		return 1;
	}

	@Override
	public final Vector normalize() {
		return this;
	}
}
