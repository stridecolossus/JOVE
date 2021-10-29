package org.sarge.jove.control;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.Rotation;
import org.sarge.jove.geometry.Rotation.MutableRotation;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>rotation animation</i> animates a rotation angle about an axis.
 * @author Sarge
 */
public class RotationAnimation implements Animation {
	private final MutableRotation rot;

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public RotationAnimation(Vector axis) {
		this.rot = new MutableRotation(axis);
	}

	/**
	 * @return Underlying rotation
	 */
	public Rotation rotation() {
		return rot;
	}

	@Override
	public void update(Animator animator) {
		final float angle = animator.position() * MathsUtil.TWO_PI;
		rot.angle(angle);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(rot).build();
	}
}
