package org.sarge.jove.control;

import java.time.Duration;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>rotation animation</i> animates a rotation angle about an axis.
 * @author Sarge
 */
public class RotationAnimation extends BoundedAnimation {
	private final MutableRotation rot;

	/**
	 * Constructor.
	 * @param axis 			Rotation axis
	 * @param period		Animation period
	 */
	public RotationAnimation(Vector axis, Duration period) {
		super(period);
		this.rot = new MutableRotation(axis);
	}

	/**
	 * @return Underlying rotation
	 */
	public Rotation rotation() {
		return rot;
	}

	@Override
	protected void update(float pos) {
		final float angle = pos * MathsUtil.TWO_PI;
		rot.angle(angle);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(rot).build();
	}
}
