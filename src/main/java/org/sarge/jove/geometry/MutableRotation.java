package org.sarge.jove.geometry;

import org.sarge.jove.animation.Animator.Animation;
import org.sarge.jove.util.Interpolator;
import org.sarge.jove.util.MathsUtil;

/**
 * Rotation specified as an axis-angle.
 * @author Sarge
 */
public class MutableRotation extends Rotation {
	private static final Interpolator INTERPOLATOR = Interpolator.interpolator(0, MathsUtil.TWO_PI, Interpolator.LINEAR);
	
	private boolean dirty;

	/**
	 * Constructor.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 */
	public MutableRotation(Vector axis, float angle) {
		super(axis, angle);
	}

	/**
	 * Constructor for zero rotation.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Vector axis) {
		this(axis, 0);
	}

	/**
	 * Sets the rotation angle.
	 * @param angle Angle (radians)
	 */
	@Override
	public void setAngle(float angle) {
		super.setAngle(angle);
		dirty = true;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	@Override
	public Matrix toMatrix() {
		dirty = false;
		return super.toMatrix();
	}

	/**
	 * Creates an animation for this rotation.
	 * @return Rotation animation
	 */
	public Animation animation() {
		return (time, duration) -> setAngle(INTERPOLATOR.interpolate(time / (float) duration));
	}
}
