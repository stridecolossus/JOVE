package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Trigonometric;

/**
 * A <i>rotation animation</i> animates a rotation angle about an axis.
 * @author Sarge
 */
public class RotationAnimation implements Animation {
	private final MutableRotation rot;

	/**
	 * Constructor given a mutable rotation.
	 * @param rot Rotation
	 */
	public RotationAnimation(MutableRotation rot) {
		this.rot = notNull(rot);
	}

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public RotationAnimation(Normal axis) {
		this(new MutableRotation(axis));
	}

	/**
	 * @return Underlying rotation as an axis-angle
	 */
	public AxisAngle rotation() {
		return rot;
	}

	@Override
	public void update(Animator animator) {
		final float angle = animator.elapsed() * Trigonometric.TWO_PI;
		rot.set(angle);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(rot).build();
	}
}
