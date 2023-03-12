package org.sarge.jove.geometry;

import org.sarge.jove.common.Layout;
import org.sarge.jove.common.Layout.Component;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>normal</i> is a unit vector with a magnitude of <b>one</b>.
 * @author Sarge
 */
public class Normal extends Vector implements Component {
	/**
	 * Layout for a vertex normal.
	 */
	public static final Layout LAYOUT = Layout.floats(3);

	/**
	 * Constructor.
	 * @param normal Normal
	 */
	public Normal(Vector normal) {
		super(normalize(normal));
	}

	/**
	 * Array constructor.
	 * @param normal Normal as a floating-point array
	 */
	public Normal(float[] normal) {
		this(new Vector(normal));
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

	@Override
	public final Layout layout() {
		return LAYOUT;
	}
}
