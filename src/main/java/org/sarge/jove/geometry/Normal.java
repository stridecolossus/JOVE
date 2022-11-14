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

	/**
	 * Array constructor.
	 */
	public Normal(float[] array) {
		this(new Vector(array));
	}

	/**
	 * Normalizes the given vector as required.
	 */
	private static Vector normalize(Vector vec) {
		final float len = vec.magnitude();
		if(MathsUtil.isEqual(1, len)) {
			return vec;
		}
		else {
    		final float f = MathsUtil.inverseRoot(len);
    		return vec.multiply(f);
		}
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
