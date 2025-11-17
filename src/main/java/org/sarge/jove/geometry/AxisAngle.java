package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.util.MathsUtility;

/**
 * An <i>axis-angle</i> specifies a mutable, counter-clockwise rotation about an arbitrary axis.
 * <p>
 * The {@link #provider()} method can be overridden to implement custom trigonometric functions.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Axis%E2%80%93angle_representation">Axis Angle Representation</a>
 * @author Sarge
 */
public class AxisAngle implements Rotation, Animation {
	private final Normal axis;
	private float angle;

	/**
	 * Constructor.
	 * @param axis 		Rotation axis
	 * @param angle		Angle (radians, counter-clockwise)
	 */
	public AxisAngle(Normal axis, float angle) {
		this.axis = requireNonNull(axis);
		this.angle = requireNonNull(angle);
	}

	/**
	 * @return Rotation axis
	 */
	public Normal axis() {
		return axis;
	}

	/**
	 * @return Rotation angle
	 */
	public float angle() {
		return angle;
	}

	@Override
	public void set(float angle) {
		this.angle = angle;
	}

	@Override
	public AxisAngle toAxisAngle() {
		return this;
	}

	/**
	 * @return Cosine provider for operations on this axis-angle
	 */
	protected Cosine.Provider provider() {
		return Cosine.Provider.DEFAULT;
	}

	/**
	 * {@inheritDoc}
	 * This method delegates to {@link Axis#rotation(float, org.sarge.jove.geometry.Cosine.Provider)} if this is a rotation about a cardinal axis.
	 */
	@Override
	public Matrix matrix() {
		if(axis instanceof Axis cardinal) {
			return cardinal.rotation(angle, provider());
		}
		else {
			return build();
		}
	}

	/**
	 * Builds the rotation matrix for this axis-angle.
	 * <p>
	 * The rotation matrix R specified by the angle A about axis N is given by:
	 * <pre>R = (cos A)I + (sin A)(N x N) + (1 - cos A)(N ^ N)</pre>
	 * where:
	 * <ul>
	 * <li>A is the angle of rotation</li>
	 * <li>N is the rotation axis</li>
	 * <li>I is the identity matrix</li>
	 * <li>{@code N x N} is the cross product matrix of the axis</li>
	 * <li>and {@code N ^ N} is the outer product</li>
	 * </ul>
	 * <p>
	 * @see <a href="https://en.wikipedia.org/wiki/Rotation_matrix">Wikipedia</a>
	 */
	private Matrix build() {
		// Init angle
		final Cosine cosine = this.provider().cosine(angle);
		final float sin = cosine.sin();
		final float cos = cosine.cos();

		// Build the identity rotation
		// TODO - use Transform.scale(cos)? => still need to handle add() to handle 3x3 and 4x4 matrices
		final Matrix identity = Matrix
				.identity(3)
				.multiply(cos);

		// Build the cross product of the axis
		final float[][] m = {
				{0, -axis.z, axis.y},
				{axis.z, 0, -axis.x},
				{-axis.y, axis.x, 0}
		};
		final Matrix cross = new Matrix(m).multiply(sin);

		// Build the outer product of the axis
		final float[] vec = axis.toArray();
		final Matrix outer = Matrix.outer(vec, vec).multiply(1 - cos);

		// Sum the components
		final Matrix rot = identity.add(cross).add(outer);

		// Convert to transform
		return new Matrix.Builder(4)
				.submatrix(0, 0, rot)
				.set(3, 3, 1)
				.build();
	}

	/**
	 * Rotates the given vector by this axis-angle.
	 * <p>
	 * This method uses <i>Rodrigues' rotation formula</i> which is expressed as:
	 * <pre>R = cos(a)v + sin(a)(n x v) + (1 - cos(a))(n.v)n</pre>
	 * Where:
	 * <ul>
	 * <li><i>a</i> is the rotation angle</li>
	 * <li><i>n</i> is the axis</li>
	 * <li>and <i>v</i> is the vector to be rotated</li>
	 * </ul>
	 * <p>
	 * This approach will generally be more efficient than constructing an intermediate rotation matrix.
	 * <p>
	 * @param v Vector to rotate
	 * @return Rotated vector
	 * @see #provider()
	 * @see <a href="https://en.wikipedia.org/wiki/Rodrigues%27_rotation_formula">Wikipedia</a>
	 */
	@Override
	public Vector rotate(Vector v) {
		final Cosine cosine = this.provider().cosine(angle);
		final float cos = cosine.cos();
		final float dot = axis.dot(v);
		final Vector a = v.multiply(cos);								// Scale the vector down
		final Vector b = axis.cross(v).multiply(cosine.sin());			// Skew towards new position
		final Vector c = axis.multiply(dot * (1 - cos));				// Restore height
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
				MathsUtility.isApproxEqual(this.angle, that.angle());
	}

	@Override
	public String toString() {
		return String.format("AxisAngle[axis=%s angle=%f]", axis, angle);
	}
}
