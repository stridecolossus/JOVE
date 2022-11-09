package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.*;

/**
 * An <i>axis-angle</i> defines a counter-clockwise rotation about an axis.
 * <p>
 * The {@link #cosine()} can be overridden to provide a custom trigonometric function used to calculate the rotation matrix.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation">Axis Angle Representation</a>
 */
public class AxisAngle implements Rotation {
	/**
	 * Helper - Constructs an axis-angle with a custom cosine function.
	 * @param rot 		Axis-angle rotation
	 * @param cosine	Cosine function
	 * @return Axis-angle
	 */
	public static AxisAngle of(AxisAngle rot, Cosine cosine) {
		return new AxisAngle(rot) {
			@Override
			public Cosine cosine() {
				return cosine;
			}
		};
	}

	private final Normal axis;
	private float angle;
	private transient Matrix matrix;

	/**
	 * Constructor.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 */
	public AxisAngle(Normal axis, float angle) {
		this.axis = notNull(axis);
		set(angle);
	}

	/**
	 * Copy constructor.
	 * @param rot Axis-angle to copy
	 */
	protected AxisAngle(AxisAngle rot) {
		this(rot.axis, rot.angle);
	}

	/**
	 * @return Rotation axis
	 */
	public final Normal axis() {
		return axis;
	}

	/**
	 * @return Rotation angle (radians)
	 */
	public final float angle() {
		return angle;
	}

	/**
	 * Provider for the cosine function used to build the rotation matrix from this axis-angle.
	 * The default implementation delegates to {@link Cosine#DEFAULT}, override to provide a custom cosine function.
	 * @return Cosine function
	 */
	public Cosine cosine() {
		return Cosine.DEFAULT;
	}

	/**
	 * Initialises the matrix for the given rotation angle.
	 * @param angle Rotation angle (radians)
	 */
	protected void set(float angle) {
		this.angle = angle;
		this.matrix = build();
	}

	/**
	 * Builds the matrix for this rotation.
	 */
	private Matrix build() {
		if(axis instanceof Axis cardinal) {
			return cardinal.rotation(angle, cosine());
		}
		else {
			return Quaternion.of(this).matrix();
		}
	}

	@Override
	public Matrix matrix() {
		return matrix;
	}

	/**
	 * Rotates the given vector by this axis-angle.
	 * <p>
	 * This method uses <i>Rodrigues' rotation formula</i> which is expressed as:
	 * <pre>rot = cos(a)v + sin(a)(n x v) + (1 - cos(a))(n.v)n</pre>
	 * Where:
	 * <ul>
	 * <li><i>a</i> is the rotation angle</li>
	 * <li><i>n</i> is the axis (or normal)</li>
	 * <li>and <i>v</i> is the vector to be rotated</li>
	 * </ul>
	 * <p>
	 * This approach may be more efficient than constructing a rotation matrix or quaternion for certain use-cases.
	 */
	@Override
	public Vector rotate(Vector vec) {
		final Cosine cosine = this.cosine();
		final float cos = cosine.cos(angle);
		final Vector a = vec.multiply(cos);
		final Vector b = axis.cross(vec).multiply(cosine.sin(angle));
		final Vector c = axis.multiply((1 - cos) * axis.dot(vec));
		return a.add(b).add(c);
	}

	@Override
	public int hashCode() {
		return Objects.hash(axis, angle);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof AxisAngle that) &&
				this.axis.equals(that.axis) &&
				MathsUtil.isEqual(this.angle, that.angle);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(axis)
				.append(angle)
				.build();
	}
}
