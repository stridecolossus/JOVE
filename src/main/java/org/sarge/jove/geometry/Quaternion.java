package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.ToString;

/**
 * Rotation represented by a quaternion.
 * @author Sarge
 */
public final class Quaternion implements Transform {
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
	 * Constructor for a counter-clockwise rotation about the given axis.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 */
	public Quaternion(Vector axis, float angle) {
		final float half = angle * MathsUtil.HALF;
		final float sin = MathsUtil.sin(half);
		this.w = MathsUtil.cos(half);
		this.x = axis.x * sin;
		this.y = axis.y * sin;
		this.z = axis.z * sin;
	}

	/**
	 * Convenience constructor.
	 * @param rot Axis-angle
	 */
	public Quaternion(Rotation rot) {
		this(rot.getAxis(), rot.getAngle());
	}

	/**
	 * Normalizes this quaternion.
	 * @return Normalized quaternion
	 */
	public Quaternion normalize() {
		final float magSquared = w * w + x * x + y * y + z * z;

		if(Math.abs(magSquared) > MathsUtil.EPSILON) {
			final float mag = 1f / MathsUtil.sqrt(magSquared);
			return new Quaternion(w * mag, x * mag, y * mag, z * mag);
		}
		else {
			return this;
		}
	}

	/**
	 * @return Conjugate of this quaternion (assuming this quaternion is normalized)
	 */
	public Quaternion conjugate() {
		return new Quaternion(w, -x, -y, -z);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public Matrix toMatrix() {
		final float xx = x * x;
		final float xy = x * y;
		final float xz = x * z;
		final float xw = x * w;
		final float yy = y * y;
		final float yz = y * z;
		final float yw = y * w;
		final float zz = z * z;
		final float zw = z * w;

		final MatrixBuilder m = new MatrixBuilder();
		
		m.set(0, 0, 1 - 2 * (yy + zz));
		m.set(1, 0, 2 * (xy + zw));
		m.set(2, 0, 2 * (xz - yw));

		m.set(0, 1, 2 * (xy - zw));
		m.set(1, 1, 1 - 2 * (xx + zz));
		m.set(2, 1, 2 * (yz + xw));

		m.set(0, 2, 2 * (xz + yw));
		m.set(1, 2, 2 * (yz - xw));
		m.set(2, 2, 1 - 2 * (xx + yy));

		m.set(3, 3, 1);

		return m.build();
	}

	/**
	 * @return This quaternion as an axis-angle rotation
	 */
	public Rotation toRotation() {
		final float scale = 1f / MathsUtil.sqrt(x * x + y * y + z * z); // TODO - is this actually required if its assumed normalised?
		final Vector axis = new Vector(x * scale, y * scale, z * scale);
		final float angle = 2f * MathsUtil.acos(w);
		return new Rotation(axis, angle);
	}

	/**
	 * Multiplies this by the given quaternion.
	 * @param q
	 * @return Result
	 */
	public Quaternion multiply(Quaternion q) {
		return new Quaternion(
				w * q.w - x * q.x - y * q.y - z * q.z,
				w * q.x + x * q.w + y * q.z - z * q.y,
				w * q.y + y * q.w + z * q.x - x * q.z,
				w * q.z + z * q.w + x * q.y - y * q.x);
	}

	/**
	 * Rotates the given vector by this quaternion.
	 * @param v Vector to rotate (assumes normalized)
	 * @return Rotated vector
	 */
	public Vector rotate(Vector v) {
		// TODO - using working values
		final Quaternion vec = new Quaternion(0, v.x, v.y, v.z);
		final Quaternion result = this.multiply(vec).multiply(this.conjugate());
		return new Vector(result.x, result.y, result.z);
	}

	// TODO - this sucks
	// TODO - change to MutablePoint and remove vector version, makes no sense to rotate a vector!
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
			if(!MathsUtil.isEqual(w, q.w)) return false;
			if(!MathsUtil.isEqual(x, q.x)) return false;
			if(!MathsUtil.isEqual(y, q.y)) return false;
			if(!MathsUtil.isEqual(z, q.z)) return false;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return ToString.toString(w, x, y, z);
	}
}
