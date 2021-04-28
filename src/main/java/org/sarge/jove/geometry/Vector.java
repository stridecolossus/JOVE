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
	 * Creates the vector between the given points.
	 * @param start		Starting point
	 * @param end		End point
	 * @return Vector between the given points
	 */
	public static Vector between(Point start, Point end) {
		final float dx = end.x() - start.x();
		final float dy = end.y() - start.y();
		final float dz = end.z() - start.z();
		return new Vector(dx, dy, dz);
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
		return dot(this);
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
	 * @return Scaled vector
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
			final float f = 1 / MathsUtil.sqrt(len);
			return scale(f);
		}
	}

	/**
	 * Calculates the <i>dot</i> (or inner, scalar) product of this and the given vector.
	 * <p>
	 * The dot product is a scalar value that expresses the angular relationship between two vectors and is calculated as follows:
	 * <p>
	 * <pre>A.B = |A| |B| cos(angle)</pre>
	 * <p>
	 * Some properties of the dot product:
	 * <ul>
	 * <li>zero if the vectors are orthogonal (i.e. perpendicular, or at right angles)</li>
	 * <li>greater than zero for an acute angle (less than 90 degree)</li>
	 * <li>negative if the angle is greater than 90 degrees</li>
	 * <li>commutative {@code a.b = b.a}</li>
	 * <li>equivalent to the cosine of the angle between two unit-vectors</li>
	 * </ul>
	 * <p>
	 * @param vec Vector
	 * @return Dot product
	 * @see <a href="https://en.wikipedia.org/wiki/Dot_product">Wikipedia</a>
	 */
	public float dot(Vector vec) {
		return x * vec.x + y * vec.y + z * vec.z;
	}

	/**
	 * Calculates the angle between this and the given vector.
	 * Assumes both vectors have been normalized.
	 * @param vec Vector
	 * @return Angle between vectors (radians)
	 * @see #dot(Vector)
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
	 * <p>
	 * The cross product is a vector perpendicular to two given vectors (and thus a normal to the plane containing them).
	 * <p>
	 * Mathematically the cross product is calculated as follows:
	 * <p>
	 * <pre>A x B = |A| |B| sin(angle) N</pre>
	 * <p>
	 * where:
	 * <br><i>angle</i> is the angle between the vectors in the plane containing A and B
	 * <br><i>N</i> is a unit-vector perpendicular to the plane (see below).
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>non-commutative</li>
	 * <li>by convention the direction of the resultant vector is given by the <i>right-hand rule</i></li>
	 * </ul>
	 * <p>
	 * @param vec Vector
	 * @return Cross product
	 * @see <a href="https://en.wikipedia.org/wiki/Cross_product#:~:text=by%20the%20symbol-,.,%2C%20engineering%2C%20and%20computer%20programming">Wikipedia</a>
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
	 * @return Projected vector
	 */
	public Vector project(Vector vec) {
		return scale(dot(vec));
	}

	/**
	 * Reflects this vector about the given normal.
	 * @param normal Normal
	 * @return Reflected vector
	 */
	public Vector reflect(Vector normal) {
		final float f = dot(normal) * -2f;
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
