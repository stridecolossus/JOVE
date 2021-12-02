package org.sarge.jove.geometry;

import org.sarge.jove.common.Layout;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>vector</i> is a direction in 3D space.
 * @author Sarge
 */
public final class Vector extends Tuple {
	/**
	 * X-axis vector.
	 */
	public static final Vector X = new Vector(1, 0, 0);

	/**
	 * Y-axis vector (note Vulkan positive Y axis is <b>down</b>).
	 */
	public static final Vector Y = new Vector(0, 1, 0);

	/**
	 * Z-axis vector (negative Z is <i>into</i> the screen).
	 */
	public static final Vector Z = new Vector(0, 0, 1);

	/**
	 * Layout of a vector (generally a normal).
	 */
	public static final Layout LAYOUT = Layout.floats(SIZE);

	/**
	 * Creates the vector between the given points, i.e. <code>end - start</code>.
	 * @param start		Starting point
	 * @param end		End point
	 * @return Vector between the given points
	 */
	public static Vector between(Point start, Point end) {
		final float dx = end.x - start.x;
		final float dy = end.y - start.y;
		final float dz = end.z - start.z;
		return new Vector(dx, dy, dz);
	}

	/**
	 * Constructor.
	 */
	public Vector(float x, float y, float z) {
		super(x, y, z);
	}

	/**
	 * Copy constructor.
	 * @param tuple Tuple to copy
	 */
	public Vector(Tuple tuple) {
		super(tuple);
	}

	/**
	 * Array constructor.
	 * @param array Vector array
	 * @throws IllegalArgumentException if the array is not comprised of three elements
	 */
	public Vector(float[] array) {
		super(array);
	}

	@Override
	public Layout layout() {
		return LAYOUT;
	}

	/**
	 * @return Magnitude (or length) <b>squared</b> of this vector
	 */
	public float magnitude() {
		return dot(this);
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
	 * Multiplies this vector by the given scalar.
	 * @param f Scalar
	 * @return Multiplied vector
	 */
	public Vector multiply(float f) {
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
			final float f = MathsUtil.inverseRoot(len);
			return multiply(f);
		}
	}

	/**
	 * Calculates the angle between this and the given vector.
	 * Assumes both vectors have been normalized.
	 * @param vec Vector
	 * @return Angle between vectors (radians)
	 * @see #dot(Tuple)
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
	 * The cross product is the vector perpendicular to this and the given vector (and thus a normal to the plane containing both).
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
	 * <li>the cross product is non-commutative</li>
	 * <li>by convention the direction of the resultant vector is determined by the <i>right-hand rule</i></li>
	 * </ul>
	 * <p>
	 * @param vec Vector
	 * @return Cross product
	 * @see <a href="https://en.wikipedia.org/wiki/Cross_product">Wikipedia</a>
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
		return multiply(dot(vec));
	}

	/**
	 * Reflects this vector about the given normal.
	 * @param normal Normal
	 * @return Reflected vector
	 */
	public Vector reflect(Vector normal) {
		final float f = dot(normal) * -2f;
		return normal.multiply(f).add(this);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof Vector vec) && super.isEqual(vec);
	}
}
