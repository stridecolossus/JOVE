package org.sarge.jove.control;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.*;
import org.sarge.jove.util.FloatSupport.FloatFunction;

/**
 * A <i>mutable rotation</i> is a specialised rotation about an axis.
 * <p>
 * This implementation selects the most performant algorithm to construct the resultant matrix depending on the rotation axis.
 * A rotation about one of the <i>cardinal</i> axes delegates to {@link Axis#rotation(float, Cosine)}.
 * Otherwise the rotation matrix calculated by {@link Quaternion#matrix()} with a new quaternion instance created on each invocation.
 * <p>
 * @author Sarge
 */
public class MutableRotation implements Rotation, Animation {
	private final Cosine cosine;
	private Normal axis;
	private float angle;
	private FloatFunction<Matrix> matrix;

	/**
	 * Constructor.
	 * @param axis			Rotation axis
	 * @param cosine		Cosine function
	 */
	public MutableRotation(Normal axis, Cosine cosine) {
		this.cosine = notNull(cosine);
		set(axis);
	}

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Normal axis) {
		this(axis, Cosine.DEFAULT);
	}

	@Override
	public Matrix matrix() {
		return matrix.apply(angle);
	}

	@Override
	public AxisAngle toAxisAngle() {
		return new AxisAngle(axis, angle);
	}

	@Override
	public Vector rotate(Vector vec, Cosine cosine) {
		return this.toAxisAngle().rotate(vec, cosine);
	}

	/**
	 * Sets the rotation axis.
	 * @param axis Rotation axis
	 */
	public void set(Normal axis) {
		this.axis = notNull(axis);
		this.matrix = function();
	}

	/**
	 * Sets the rotation angle.
	 * @param angle Rotation angle (radians)
	 */
	public void set(float angle) {
		this.angle = angle;
	}

	/**
	 * Determines the appropriate rotation function for the given axis.
	 */
	private FloatFunction<Matrix> function() {
		if(axis instanceof Axis cardinal) {
			return angle -> cardinal.rotation(angle, cosine);
		}
		else {
			return angle -> {
				final AxisAngle rot = this.toAxisAngle();
				return Quaternion.of(rot, cosine).matrix();
			};
		}
	}

	@Override
	public void update(float pos) {
		final float angle = pos * Trigonometric.TWO_PI;
		set(angle);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(axis)
				.append(angle)
				.build();
	}
}
