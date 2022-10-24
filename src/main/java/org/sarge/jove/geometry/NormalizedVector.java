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
	public final NormalizedVector normalize() {
		return this;
	}

	@Override
	public NormalizedVector invert() {
		// TODO - consider cached inverse vector => make vector sealed and this final (also then supports axis vector)
		return new NormalizedVector(super.invert());
	}
}
