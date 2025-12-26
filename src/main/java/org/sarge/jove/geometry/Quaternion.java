package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.MathsUtility.*;

import org.sarge.jove.util.MathsUtility;

/**
 * A <i>quaternion</i> is a more compact and efficient representation of a rotation about an arbitrary axis.
 * <p>
 * This implementation represents a <i>rotation quaternion</i> which is also implicitly a unit quaternion.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation">Wikipedia</a>
 * @author Sarge
 */
public record Quaternion(float scalar, Vector vector) implements Transform {
	/**
	 * Quaternion that represents no rotation.
	 */
	public static final Quaternion IDENTITY = new Quaternion(1, new Vector(0, 0, 0));

	/**
	 * Creates a quaternion from the given axis-angle rotation.
	 * @param rotation Axis-angle rotation
	 * @return Rotation quaternion
	 */
	public static Quaternion of(AxisAngle rotation) {
		final float half = rotation.angle() * HALF;
		final Cosine cosine = rotation.provider().cosine(half);
		final Vector axis = rotation.axis().multiply(cosine.sin());
		return new Quaternion(cosine.cos(), axis);
	}

	/**
	 * Constructor.
	 * @param scalar Scalar component
	 * @param vector Vector component
	 */
	public Quaternion {
		requireNonNull(vector);
	}
	// TODO - assumes but does not enforce normalised (as things stand)

	/**
	 * Converts this quaternion to an axis-angle rotation.
	 * The rotation axis is undefined if this is approximately the {@link #IDENTITY} quaternion
	 * @param provider Cosine function
	 * @return Axis-angle
	 */
	public AxisAngle toAxisAngle(Cosine.Provider provider) {
		// TODO - numerically unstable when w near +/- 1 (???)
		final float angle = 2 * (float) Math.acos(scalar);
		final float sin = provider.cosine(angle * HALF).sin();
		final Vector axis = vector.multiply(1 / sin);
		return new AxisAngle(new Normal(axis), angle, provider);
	}

	/**
	 * Calculates the conjugate of this quaternion.
	 * Note that the conjugate of a rotation quaternion is also its inverse.
	 * @return Conjugate
	 */
	public Quaternion conjugate() {
		return new Quaternion(scalar, vector.invert());
	}

	/**
	 * Multiplies this and the given quaternion.
	 * <p>
	 * The resultant quaternion is determined as follows:
	 * <pre>scalar = w1 * w2 - v1 . v2</pre>
	 * and
	 * <pre>vector = w1 * v2 + w2 * v1 + v1 x v2</pre>
	 * <p>
	 * Quaternion multiplication is:
	 * <ul>
	 * <li>associative: {@code (ab)c = a(bc)}</li>
	 * <li>non commutative: {@code ab != ba}</li>
	 * </ul>
	 * <p>
	 * @param that Quaternion to multiply
	 * @return Multiplied quaternion
	 */
	public Quaternion multiply(Quaternion that) {
		final float w = this.scalar * that.scalar - this.vector.dot(that.vector);
		final Vector a = this.vector.multiply(that.scalar);
		final Vector b = that.vector.multiply(this.scalar);
		final Vector cross = this.vector.cross(that.vector);
		final Vector total = a.add(b).add(cross);
		return new Quaternion(w, total);
	}

	/**
	 * Rotates the given vector about this rotation.
	 * @param vector Vector to rotate
	 * @return Rotated vector
	 */
	public Vector rotate(Vector vector) {
		final Quaternion q = new Quaternion(0, vector);
		final Quaternion result = this.multiply(q).multiply(this.conjugate());
		assert isApproxZero(result.scalar);
		return result.vector;
	}

	// TODO - slerp
	// https://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/slerp/index.htm

	/**
	 * @return This quaternion as a rotation matrix
	 */
	@Override
	public Matrix matrix() {
		final float xx = vector.x * vector.x;
		final float xy = vector.x * vector.y;
		final float xz = vector.x * vector.z;
		final float xw = vector.x * scalar;
		final float yy = vector.y * vector.y;
		final float yz = vector.y * vector.z;
		final float yw = vector.y * scalar;
		final float zz = vector.z * vector.z;
		final float zw = vector.z * scalar;

		return new Matrix.Builder(4)
    			.set(0, 0, 1 - 2 * (yy + zz))
    			.set(1, 0, 2 * (xy + zw))
    			.set(2, 0, 2 * (xz - yw))
    			.set(0, 1, 2 * (xy - zw))
    			.set(1, 1, 1 - 2 * (xx + zz))
    			.set(2, 1, 2 * (yz + xw))
    			.set(0, 2, 2 * (xz + yw))
    			.set(1, 2, 2 * (yz - xw))
    			.set(2, 2, 1 - 2 * (xx + yy))
    			.set(3, 3, 1)
    			.build();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Quaternion that) &&
				MathsUtility.isApproxEqual(this.scalar, that.scalar) &&
				vector.equals(that.vector);
	}
}
