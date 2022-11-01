package org.sarge.jove.geometry;

import java.util.Arrays;

import org.sarge.jove.util.MathsUtil;

/**
 * A <i>quaternion</i> is a more compact and efficient representation of a rotation about an arbitrary axis.
 * @see <a href="https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation">Wikipedia</a>
 * @author Sarge
 */
public final class Quaternion implements Rotation {
	/**
	 * Identity quaternion.
	 */
	public static final Quaternion IDENTITY = new Quaternion(1, 0, 0, 0);

	/**
	 * Creates a quaternion from the given axis-angle rotation.
	 * @param rot Axis-angle
	 * @return New quaternion
	 */
	public static Quaternion of(AxisAngle rot) {
		final float half = rot.angle() * MathsUtil.HALF;
		final float w = MathsUtil.cos(half);
		final Vector vec = rot.axis().multiply(MathsUtil.sin(half));
		return new Quaternion(w, vec);
	}

	public final float w, x, y, z;

	/**
	 * Constructor.
	 */
	public Quaternion(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Constructor given an axis.
	 */
	public Quaternion(float w, Vector axis) {
		this(w, axis.x, axis.y, axis.z);
	}

	/**
	 * @return Magnitude <b>squared</b> of this quaternion
	 */
	public float magnitude() {
		return w * w + x * x + y * y + z * z;
	}

	/**
	 * @return Components of this quaternion as an array
	 */
	public float[] array() {
		return new float[]{w, x, y, z};
	}

	/**
	 * @return Axis components as a vector
	 */
	private Vector axis() {
		return new Vector(x, y, z);
	}

	@Override
	public AxisAngle toAxisAngle() {
		final float scale = MathsUtil.inverseRoot(1 - w * w);
		final float angle = 2 * MathsUtil.acos(w);
		final Vector axis = this.axis().multiply(scale);
		return AxisAngle.of(axis, angle);
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
			final float mag = MathsUtil.inverseRoot(magnitude);
			final Vector vec = this.axis().multiply(mag);
			return new Quaternion(w * mag, vec);
		}
	}

	/**
	 * @return Conjugate of this quaternion (assumes normalized)
	 */
	public Quaternion conjugate() {
		return new Quaternion(w, this.axis().invert());
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

	@Override
	public Vector rotate(Vector vec) {
		final Quaternion q = new Quaternion(0, vec);
		final Quaternion result = this.multiply(q).multiply(this.conjugate());
		return result.axis();
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
				MathsUtil.isEqual(this.array(), that.array());
	}

	@Override
	public String toString() {
		return Arrays.toString(array());
	}
}
