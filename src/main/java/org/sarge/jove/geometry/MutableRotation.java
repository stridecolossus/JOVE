package org.sarge.jove.geometry;

import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.util.Trigonometric;

/**
 * Mutable implementation.
 * @author Sarge
 */
public class MutableRotation extends AxisAngle implements Animation {
	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Normal axis) {
		super(axis, 0);
	}

	@Override
	public void set(float angle) {
		super.set(angle);
	}

	@Override
	public final boolean isMutable() {
		return true;
	}

	@Override
	public void update(float pos) {
		final float angle = pos * Trigonometric.TWO_PI;
		set(angle);
	}
}
