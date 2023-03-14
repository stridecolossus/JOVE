package org.sarge.jove.geometry;

import org.sarge.jove.util.Cosine;
import org.sarge.lib.util.Check;

/**
 * An <i>axis-angle</i> is the default implementation for a rotation.
 * <p>
 * The {@link #cosine()} method can be overridden to provide a custom trigonometric function used to calculate the rotation matrix.
 * Alternatively see {@link #of(AxisAngle, Cosine)}
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation">Axis Angle Representation</a>
 * @author Sarge
 */
public record AxisAngle(Normal axis, float angle) implements Rotation {
	/**
	 * Constructor.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 */
	public AxisAngle {
		Check.notNull(axis);
	}

	@Override
	public final Matrix matrix() {
		return Quaternion.of(this).matrix();
	}

	@Override
	public AxisAngle toAxisAngle() {
		return this;
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
	public Vector rotate(Vector vec, Cosine cosine) {
		final float cos = cosine.cos(angle);
		final Vector a = vec.multiply(cos);
		final Vector b = axis.cross(vec).multiply(cosine.sin(angle));
		final Vector c = axis.multiply((1 - cos) * axis.dot(vec));
		return a.add(b).add(c);
	}
}
