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
	 * @param axis			Rotation axis
	 * @param angle			Initial angle (radians)
	 */
	public RotationAnimation(Vector axis, float angle) {
		this.rot = new MutableRotation(axis);
		rot.angle(angle);
	}

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public RotationAnimation(Vector axis) {
		this(axis, 0);
	}

	/**
	 * @return Rotation
	 */
	public Rotation rotation() {
		return rot;
	}

	@Override
	public void update(Animator animator) {
		rot.angle(animator.position() * MathsUtil.TWO_PI);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(rot).build();
	}
}
