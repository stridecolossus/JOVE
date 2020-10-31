package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;

/**
 * A <i>vector</i> is a direction in 3D space.
 * @author Sarge
 */
public final class Vector extends Tuple {
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
	public static Vector of(Point start, Point end) {
		return new Vector(end.x - start.x, end.y - start.y, end.z - start.z);
	}

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector(float x, float y, float z) {
		super(x, y, z);
	}

	/**
	 * Array constructor.
	 * @param array Vector as an array
	 */
	public Vector(float[] array) {
		super(array);
	}

	/**
	 * Copy constructor.
	 * @param tuple Tuple
	 */
	public Vector(Tuple tuple) {		// TODO - protected?
		super(tuple);
	}

	/**
	 * @return Magnitude (<b>squared</b>) of this vector
	 */
	public float magnitude() {
		return (x * x) + (y * y) + (z * z);
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
}
