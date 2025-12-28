package org.sarge.jove.geometry;

import org.sarge.jove.common.Layout;
import org.sarge.jove.common.Layout.Type;

/**
 * A <i>normal</i> is a unit vector.
 * @author Sarge
 */
public class Normal extends Vector {
	/**
	 * Vertex normal layout.
	 */
	public static final Layout LAYOUT = new Layout(SIZE, Type.NORMALIZED, true, Float.BYTES);

	/**
	 * Constructor.
	 * @param vector Vector to normalise
	 */
	public Normal(Vector vector) {
		super(vector.normalize());
	}

	public Normal(float x, float y, float z) {
		this(new Vector(x, y, z));
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
