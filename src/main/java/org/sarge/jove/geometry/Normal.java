package org.sarge.jove.geometry;

import org.sarge.jove.common.Component;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>normal</i> is a unit vector with a magnitude of <b>one</b>.
 * @author Sarge
 */
public sealed class Normal extends Vector permits Axis {
	/**
	 * Layout for a vertex normal.
	 */
	public static final Component LAYOUT = Component.floats(3);

	/**
	 * Constructor.
	 * @param vec Vector
	 */
	public Normal(Vector vec) {
		super(normalize(vec));
	}

	private static Vector normalize(Vector vec) {
		// Check for copy
		if(vec instanceof Normal) {
			return vec;
		}

		// Check for arbitrary unit-vector
		final float len = vec.magnitude();
		if(MathsUtil.isEqual(1, len)) {
			return vec;
		}

		// Otherwise normalise vector
		final float f = MathsUtil.inverseRoot(len);
		return vec.multiply(f);
	}

	@Override
	public final float magnitude() {
		return 1;
	}

	@Override
	public final Normal normalize() {
		return this;
	}

	@Override
	public Normal invert() {
		return new Normal(super.invert());
	}
}
