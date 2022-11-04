package org.sarge.jove.geometry;

import org.sarge.jove.common.Component;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>normal</i> is a unit vector (magnitude of <b>one</b>).
 * @author Sarge
 */
public sealed class Normal extends Vector permits Axis {
	/**
	 * Layout for a vertex normal.
	 */
	public static final Component LAYOUT = Component.floats(3);

	/**
	 * Creates a normalized vector.
	 * @param vec Vector
	 * @return Normal
	 * @see Vector#normalize()
	 */
	public static Normal of(Vector vec) {
		return new Normal(vec.normalize());
	}

	/**
	 * Constructor.
	 * @param normal Normal vector
	 */
	protected Normal(Vector normal) {
		super(normal);
		assert MathsUtil.isEqual(1, normal.magnitude());
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
