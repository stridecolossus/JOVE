package org.sarge.jove.geometry;

import static org.sarge.jove.util.MathsUtil.cos;
import static org.sarge.jove.util.MathsUtil.inverseRoot;
import static org.sarge.jove.util.MathsUtil.isEqual;
import static org.sarge.jove.util.MathsUtil.sin;

import java.util.Arrays;

import org.sarge.jove.geometry.Rotation.AbstractRotation;
import org.sarge.jove.util.MathsUtil;

/**
 * Rotation represented by a quaternion.
 * @author Sarge
 */
public final class Quaternion implements Transform {
	/**
	 * Identity quaternion.
	 */
	public static final Quaternion IDENTITY = new Quaternion(1, 0, 0, 0);

	/**
	 * Creates a quaternion from the given rotation.
	 * @param rot Rotation
	 */
	public static Quaternion of(Vector axis, float angle) {
		final float half = angle * MathsUtil.HALF;
		final Vector vec = axis.multiply(sin(half));
		return new Quaternion(cos(half), vec.x, vec.y, vec.z);
	}

	public final float w, x, y, z;

	/**
	 * Constructor.
	 * @param w
	 * @param x
	 * @param y
	 * @param z
	 */
	public Quaternion(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * @return Magnitude <b>squared</b> of this quaternion
	 */
	public float magnitude() {
		return w * w + x * x + y * y + z * z;
	}

	/**
	 * Converts this quaternion to a rotation transform (assumes normalized).
	 * @return Rotation
	 */
	public Rotation rotation() {
		// Extract axis-angle
		final float scale = inverseRoot(1 - w * w);
		final Vector axis = new Vector(x, y, z).multiply(scale);
		final float angle = 2 * MathsUtil.acos(w);

		// Create rotation wrapper
		return new AbstractRotation(axis, angle) {
			@Override
			public Matrix matrix() {
				return Quaternion.this.matrix();
			}
		};
	}

	/**
	 * Normalizes this quaternion.
	 * @return Normalized quaternion
	 */
	public Quaternion normalize() {
		final float magnitude = magnitude();
		if(MathsUtil.isZero(magnitude)) {
			return this;
		}
		else {
			final float mag = inverseRoot(magnitude);
			return new Quaternion(w * mag, x * mag, y * mag, z * mag);
		}
	}

	/**
	 * @return Conjugate of this quaternion (assumes normalized)
	 */
	public Quaternion conjugate() {
		return new Quaternion(w, -x, -y, -z);
	}

	/**
	 * Multiplies by the given quaternion.
	 * @param q Quaternion
	 * @return New quaternion
	 */
	public Quaternion multiply(Quaternion q) {
		return new Quaternion(
			w * q.w - x * q.x - y * q.y - z * q.z,
			w * q.x + x * q.w + y * q.z - z * q.y,
			w * q.y + y * q.w + z * q.x - x * q.z,
			w * q.z + z * q.w + x * q.y - y * q.x
		);
	}

	/**
	 * Rotates the given vector by this quaternion.
	 * @param vec Vector
	 * @return Rotated vector
	 */
	public Vector rotate(Vector vec) {
		final Quaternion q = new Quaternion(0, vec.x, vec.y, vec.z);
		final Quaternion result = this.multiply(q).multiply(this.conjugate());
		return new Vector(result.x, result.y, result.z);
	}

	// TODO - slerp
	// https://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/slerp/index.htm

	@Override
	public Matrix matrix() {
		final float xx = x * x;
		final float xy = x * y;
		final float xz = x * z;
		final float xw = x * w;
		final float yy = y * y;
		final float yz = y * z;
		final float yw = y * w;
		final float zz = z * z;
		final float zw = z * w;

		return new Matrix.Builder()
			.identity()
			.set(0, 0, 1 - 2 * (yy + zz))
			.set(1, 0, 2 * (xy + zw))
			.set(2, 0, 2 * (xz - yw))
			.set(0, 1, 2 * (xy - zw))
			.set(1, 1, 1 - 2 * (xx + zz))
			.set(2, 1, 2 * (yz + xw))
			.set(0, 2, 2 * (xz + yw))
			.set(1, 2, 2 * (yz - xw))
			.set(2, 2, 1 - 2 * (xx + yy))
			.build();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Quaternion that) &&
				isEqual(this.w, that.w) &&
				isEqual(this.x, that.x) &&
				isEqual(this.y, that.y) &&
				isEqual(this.z, that.z);
	}

	@Override
	public String toString() {
		return Arrays.toString(new float[]{w, x, y, z});
	}
}
