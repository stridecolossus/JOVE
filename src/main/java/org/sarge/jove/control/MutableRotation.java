package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.*;

/**
 * A <i>mutable rotation</i> is a specialised rotation about an axis.
 * @author Sarge
 */
public class MutableRotation implements Rotation, Animation {
	private AxisAngle rot;

	/**
	 * Constructor.
	 * @param rot Rotation
	 */
	public MutableRotation(AxisAngle rot) {
		this.rot = notNull(rot);
	}

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Normal axis) {
		this(new AxisAngle(axis, 0));
	}

	@Override
	public Matrix matrix() {
		return rot.matrix();
	}

	@Override
	public AxisAngle toAxisAngle() {
		return rot;
	}

	@Override
	public Vector rotate(Vector vec) {
		return rot.rotate(vec);
	}

	/**
	 * Sets the rotation axis.
	 * @param axis Rotation axis
	 */
	public void set(Normal axis) {
		set(axis, rot.angle());
	}

	/**
	 * Sets the rotation angle.
	 * @param angle Rotation angle (radians)
	 */
	public void set(float angle) {
		set(rot.axis(), angle);
	}

	/**
	 * Resets the underlying rotation.
	 */
	private void set(Normal axis, float angle) {
		final var updated = new AxisAngle(axis, angle);
		final Cosine cos = rot.cosine();
		rot = updated.of(cos);
	}

	@Override
	public void update(float pos) {
		final float angle = pos * Trigonometric.TWO_PI;
		set(angle);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(rot).build();
	}
}
