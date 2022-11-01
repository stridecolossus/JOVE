package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;

/**
 * A <i>normalized vector</i> is a unit vector (magnitude of <b>one</b>).
 * @author Sarge
 */
public sealed class NormalizedVector extends Vector permits Axis {
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
	public final NormalizedVector normalize() {
		return this;
	}

	@Override
	public NormalizedVector invert() {
		return new NormalizedVector(super.invert());
	}
}
