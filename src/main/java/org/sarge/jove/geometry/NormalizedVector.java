package org.sarge.jove.geometry;

/**
 * A <i>normalized vector</i> is a unit vector with a magnitude of <b>one</b>.
 * @author Sarge
 */
public class NormalizedVector extends Vector {
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
	public Vector normalize() {
		return this;
	}
}
