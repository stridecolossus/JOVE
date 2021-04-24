package org.sarge.jove.geometry;

import java.nio.ByteBuffer;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Component;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>vector</i> is a direction in 3D space.
 * @author Sarge
 */
public record Vector(float x, float y, float z) implements Bufferable, Component {
	/**
	 * X-axis vector.
	 */
	public static final Vector X_AXIS = new Vector(1, 0, 0);

	/**
	 * Y-axis vector (note Vulkan positive Y axis is <b>down</b>).
	 */
	public static final Vector Y_AXIS = new Vector(0, 1, 0);

	/**
	 * Z-axis vector (negative Z is <i>into</i> the screen).
	 */
	public static final Vector Z_AXIS = new Vector(0, 0, 1);

	/**
	 * Creates a vector between the given points.
	 * @param start		Starting point
	 * @param end		End point
	 * @return Vector between the given points
	 */
	public static Vector between(Point start, Point end) {
		return start.toVector().invert().add(end.toVector());
	}

	/**
	 * Creates a vector from the given array.
	 * @param array Vector array
	 * @return New vector
	 * @throws IllegalArgumentException if the array is not comprised of three elements
	 */
	public static Vector of(float[] array) {
		if(array.length != 3) throw new IllegalArgumentException("Invalid array length: " + array.length);
		final float x = array[0];
		final float y = array[1];
		final float z = array[2];
		return new Vector(x, y, z);
	}

	/**
	 * @return Magnitude (or length <b>squared</b>) of this vector
	 */
	public float magnitude() {
		return (x * x) + (y * y) + (z * z);
	}

	/**
	 * @return This vector as a point relative to the origin
	 */
	public Point toPoint() {
		return new Point(x, y, z);
	}

	/**
	 * @return Inverse of this vector
	 */
	public Vector invert() {
		return new Vector(-x, -y, -z);
	}

	/**
	 * Adds the given vector to this vector.
	 * @param vec Vector to add
	 * @return Added vector
	 */
	public Vector add(Vector vec) {
		return new Vector(x + vec.x, y + vec.y, z + vec.z);
	}

	/**
	 * Scales this vector.
	 * @param f Scalar
	 * @return New scaled vector
	 */
	public Vector scale(float f) {
		return new Vector(x * f, y * f, z * f);
	}

	/**
	 * @return Normalized (or unit) vector
	 */
	public Vector normalize() {
		final float len = magnitude();
		if(MathsUtil.isEqual(1, len)) {
			return this;
		}
		else {
			final float f = 1f / MathsUtil.sqrt(len);
			return scale(f);
		}
	}

	/**
	 * Calculates the dot (or scalar) product of this and the given vector.
	 * @param vec Vector
	 * @return Dot product
	 */
	public float dot(Vector vec) {
		// TODO - |v| * |u| * cos(angle)
		return x * vec.x + y * vec.y + z * vec.z;
	}

	/**
	 * Calculates the angle between this and the given vector.
	 * Assumes both vectors have been normalized.
	 * @param vec Vector
	 * @return Angle between vectors (radians)
	 */
	public float angle(Vector vec) {
		final float dot = dot(vec);
		if(dot < -1) {
			return -1;
		}
		else
		if(dot > 1) {
			return 1;
		}
		else {
			return MathsUtil.acos(dot);
		}
	}

	/**
	 * Calculates the <i>cross product</i> of this and the given vector.
	 * Assumes both vectors have been normalized.
	 * @param vec Vector
	 * @return Cross product
	 */
	public Vector cross(Vector vec) {
		final float x = this.y * vec.z - this.z * vec.y;
		final float y = this.z * vec.x - this.x * vec.z;
		final float z = this.x * vec.y - this.y * vec.x;
		return new Vector(x, y, z);
	}

	/**
	 * Projects the given vector onto this vector.
	 * @param vec Vector to project
	 * @return Projected vector as a tuple
	 */
	public Vector project(Vector vec) {
		return scale(this.dot(vec));
	}

	/**
	 * Reflects this vector about the given normal.
	 * @param normal Normal
	 * @return Reflected vector
	 */
	public Vector reflect(Vector normal) {
		final float f = this.dot(normal) * -2f;
		return normal.scale(f).add(this);
	}

	@Override
	public Layout layout() {
		return Layout.TUPLE;
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		buffer.putFloat(x).putFloat(y).putFloat(z);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj instanceof Vector that) &&
				MathsUtil.isEqual(this.x, that.x) &&
				MathsUtil.isEqual(this.y, that.y) &&
				MathsUtil.isEqual(this.z, that.z);
	}
}
