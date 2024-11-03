package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.MathsUtility.*;

import java.util.Objects;

import org.sarge.jove.geometry.Cosine.Provider;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>quaternion</i> is a more compact and efficient representation of a rotation about an arbitrary axis.
 * <p>
 * This implementation represents a <i>rotation quaternion</i> which is implicitly a unit quaternion.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation">Wikipedia</a>
 * @author Sarge
 */
public class Quaternion implements Transform {
	/**
	 * Quaternion that represents no rotation.
	 * @see #toAxisAngle()
	 */
	public static final Quaternion IDENTITY = new Quaternion(1, new Vector(0, 0, 0));

	/**
	 * Creates a quaternion from the given axis-angle rotation.
	 * @param rotation Axis-angle rotation
	 * @return Rotation quaternion
	 */
	public static Quaternion of(AxisAngle rotation) {
		final Angle angle = rotation.angle();
		final float half = angle.angle() * HALF;
		final Cosine.Provider provider = angle.provider();
		final Cosine cosine = provider.cosine(half);
		final Vector axis = rotation.axis().multiply(cosine.sin());
		return new Quaternion(cosine.cos(), axis) {
			@Override
			protected Provider provider() {
				return provider;
			}
		};
	}

	private final float scalar;
	private final Vector vector;

	/**
	 * Constructor.
	 * @param scalar Scalar component
	 * @param vector Vector component
	 */
	protected Quaternion(float scalar, Vector vector) {
		this.scalar = scalar;
		this.vector = requireNonNull(vector);
	}
	// TODO - assumes but does not enforce normalised (as things stand)

	/**
	 * @return Scalar component
	 */
	public float scalar() {
		return scalar;
	}

	/**
	 * @return Vector component
	 */
	public Vector vector() {
		return vector;
	}

	/**
	 * Converts this quaternion to an axis-angle rotation.
	 * The rotation axis is undefined if this is approximately the {@link #IDENTITY} quaternion
	 * @return Axis-angle
	 */
	public AxisAngle toAxisAngle() {
		// TODO - numerically unstable when w near +/- 1 (???)
		final float angle = 2 * (float) Math.acos(scalar); // TODO - maths
		final float sin = this.provider().cosine(angle * HALF).sin();
		final Vector axis = vector.multiply(1 / sin);
		return new AxisAngle(new Normal(axis), angle);
	}

	/**
	 * @return Cosine function
	 */
	protected Cosine.Provider provider() {
		return Cosine.Provider.DEFAULT;
	}

	/**
	 * Note that the conjugate of a rotation quaternion is its inverse.
	 * @return Conjugate of this quaternion
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
	 * Rotates the given vector by this quaternion.
	 * @param v Vector to rotate
	 * @return Rotated vector
	 */
	public Vector rotate(Vector v) {
		final Quaternion q = new Quaternion(0, v);
		final Quaternion result = this.multiply(q).multiply(this.conjugate());
		assert isApproxZero(result.scalar);
		return result.vector;
	}

	// TODO - slerp
	// https://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/slerp/index.htm

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
	public int hashCode() {
		return Objects.hash(scalar, vector);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Quaternion that) &&
				MathsUtility.isApproxEqual(this.scalar, that.scalar) &&
				vector.equals(that.vector);
	}

	@Override
	public String toString() {
		final String w = MathsUtility.FORMATTER.format(scalar);
		return String.format("Quaternion[%s, %s]", w, vector);
	}
}
