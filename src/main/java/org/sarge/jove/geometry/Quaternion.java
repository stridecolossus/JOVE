package org.sarge.jove.geometry;

import java.util.Arrays;

import org.sarge.jove.util.MathsUtil;

/**
 * Rotation represented by a quaternion.
 * @author Sarge
 */
public final class Quaternion implements Transform {
	/**
	 * Constructor for a <b>counter-clockwise</b> rotation.
	 * @param rot Rotation
	 */
	public static Quaternion of(Rotation rot) {
		final Vector axis = rot.axis();
		final float half = rot.angle() * MathsUtil.HALF;
		final float sin = MathsUtil.sin(half);
		return new Quaternion(MathsUtil.cos(half), axis.x * sin, axis.y * sin, axis.z * sin);
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
	 * @return Magnitude of this quaternion
	 */
	public float magnitude() {
		return w * w + x * x + y * y + z * z;
	}

	/**
	 * Converts this quaternion to a rotation transform.
	 * @return Rotation
	 */
	public Rotation toRotation() {
		// TODO - scale could divide-by-zero?
		final float scale = 1f / MathsUtil.sqrt(x * x + y * y + z * z); // TODO - is this actually required if its assumed normalised?
		final Vector axis = new Vector(x * scale, y * scale, z * scale);
		final float angle = 2f * MathsUtil.acos(w);
		return Rotation.of(axis, angle);
	}

	@Override
	public Matrix matrix() {
		// https://sites.google.com/site/glennmurray/Home/rotation-matrices-and-formulas/rotation-about-an-arbitrary-axis-in-3-dimensions
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

	/**
	 * Normalises this quaternion.
	 * @return Normalized quaternion
	 */
	public Quaternion normalize() {
		final float magnitude = magnitude();
		if(MathsUtil.isZero(magnitude)) {
			return this;
		}
		else {
			final float mag = 1f / MathsUtil.sqrt(magnitude);
			return new Quaternion(w * mag, x * mag, y * mag, z * mag);
		}
	}

	/**
	 * @return Conjugate of this quaternion (assumes this quaternion is normalized)
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
	 * Rotates the given point about this quaternion.
	 * @param pos Point to rotate
	 * @return Rotated point
	 */
	public Point rotate(Point pos) {
		final Quaternion vec = new Quaternion(0, pos.x, pos.y, pos.z);
		final Quaternion result = this.multiply(vec).multiply(this.conjugate());
		return new Point(result.x, result.y, result.z);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(obj instanceof Quaternion) {
			final Quaternion q = (Quaternion) obj;
			if(!MathsUtil.equals(w, q.w)) return false;
			if(!MathsUtil.equals(x, q.x)) return false;
			if(!MathsUtil.equals(y, q.y)) return false;
			if(!MathsUtil.equals(z, q.z)) return false;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return Arrays.toString(new float[]{w, x, y, z});
	}
}
