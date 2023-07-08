package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.*;

/**
 * An <i>axis-angle</i> is the default implementation for a rotation.
 * <p>
 * The {@link #matrix()} method selects the optimal algorithm to construct a matrix from this rotation.
 * A rotation about a cardinal {@link Axis} delegates to {@link Axis#rotation(float, Cosine)}.
 * Otherwise the matrix is constructed from a {@link Quaternion} instance created on each invocation.
 * <p>
 * The {@link #cosine()} method can be overridden to provide a custom trigonometric function used to calculate the rotation matrix.
 * Alternatively see {@link #of(Cosine)}.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation">Axis Angle Representation</a>
 * @author Sarge
 */
public class AxisAngle implements Rotation {
	private final Normal axis;
	private final float angle;

	/**
	 * Constructor.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 */
	public AxisAngle(Normal axis, float angle) {
		this.axis = notNull(axis);
		this.angle = angle;
	}

	/**
	 * Copy constructor.
	 */
	protected AxisAngle(AxisAngle that) {
		this(that.axis(), that.angle());
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

	@Override
	public final Matrix matrix() {
		if(axis instanceof Axis cardinal) {
			return cardinal.rotation(angle, cosine());
		}
		else {
			return Quaternion.of(this).matrix();
		}
	}

	@Override
	public final AxisAngle toAxisAngle() {
		return this;
	}

	/**
	 * Override for a custom cosine function.
	 * @return Cosine function
	 */
	public Cosine cosine() {
		return Cosine.DEFAULT;
	}

	/**
	 * Creates an adapter for this axis-angle that uses the given cosine function when constructing the rotation matrix.
	 * @param cosine Cosine function
	 * @return Axis-angle
	 */
	public AxisAngle of(Cosine cosine) {
		return new AxisAngle(this) {
			@Override
			public Cosine cosine() {
				return cosine;
			}
		};
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
	 * <p>
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
				this.axis.equals(that.axis()) &&
				MathsUtil.isEqual(this.angle, that.angle());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(axis).append(angle).build();
	}
}
