package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtility;

/**
 * A <i>vector</i> is a direction in 3D space.
 * @author Sarge
 */
public class Vector extends Tuple {
	/**
	 * Creates the vector between the given points, i.e. {@code end - start}.
	 * @param start		Starting point
	 * @param end		End point
	 * @return Vector between the given points
	 */
	public static Vector between(Point start, Point end) {
		final float x = end.x - start.x;
		final float y = end.y - start.y;
		final float z = end.z - start.z;
		return new Vector(x, y, z);
	}

	/**
	 * Constructor.
	 */
	public Vector(float x, float y, float z) {
		super(x, y, z);
	}

	/**
	 * Copy constructor.
	 * @param that Vector to copy
	 */
	public Vector(Tuple that) {
		super(that);
	}

	/**
	 * Array constructor.
	 * @param Vector array
	 * @throws ArrayIndexOutOfBoundsException if the array does not have three elements
	 */
	public Vector(float[] array) {
		super(array);
	}

	/**
	 * @return Magnitude (or length) <b>squared</b> of this vector
	 */
	public float magnitude() {
		return dot(this);
	}

	/**
	 * Calculates the <i>dot</i> product of two vectors.
	 * <p>
	 * The dot product (also known as the <i>inner</i> or <i>scalar</i> vector product) expresses the angular relationship between two vectors.
	 * This is represented mathematically as:
	 * <p>
	 * <pre>A.B = |A| |B| cos(angle)</pre>
	 * <p>
	 * The dot product is:
	 * <ul>
	 * <li>zero if the vectors are orthogonal, i.e. perpendicular or at right angles</li>
	 * <li>greater than zero for an acute angle (less than 90 degree)</li>
	 * <li>negative if the angle is greater than 90 degrees</li>
	 * <li>commutative, i.e. {@code a.b = b.a}</li>
	 * <li>proportional to the cosine of the angle between two unit-vectors</li>
	 * <li>the <i>magnitude</i> of a vector when applied to itself</li>
	 * </ul>
	 * <p>
	 * @param that Vector
	 * @return Dot product
	 * @see <a href="https://en.wikipedia.org/wiki/Dot_product">Wikipedia</a>
	 */
	public float dot(Vector that) {
		return this.x * that.x + this.y * that.y + this.z * that.z;
	}

	/**
	 * @return Inverse of this vector
	 */
	public Vector invert() {
		return new Vector(-x, -y, -z);
	}

	/**
	 * Adds the given vector to this vector.
	 * @param that Vector to add
	 * @return Added vector
	 */
	public final Vector add(Vector that) {
		return new Vector(
				this.x + that.x,
				this.y + that.y,
				this.z + that.z
		);
	}

	/**
	 * Multiplies this vector by the given scalar.
	 * @param f Scalar
	 * @return Scaled vector
	 */
	public final Vector multiply(float f) {
		return new Vector(x * f, y * f, z * f);
	}

	/**
	 * @return Normalized (or unit) vector
	 * @see Normal
	 */
	public Vector normalize() {
		final float len = this.magnitude();
		if(MathsUtility.isApproxEqual(1, len)) {
			return this;
		}
		else {
    		final float f = MathsUtility.inverseSquareRoot(len);
    		return multiply(f);
		}
	}

	/**
	 * Calculates the angle between this and the given vector.
	 * @param that Vector
	 * @return Angle between the vectors (radians)
	 * @see #dot(Vector)
	 */
	public final float angle(Vector that) {
		final float dot = this.dot(that);
		if(dot < -1) {
			return MathsUtility.PI;
		}
		else
		if(dot > 1) {
			return 0;
		}
		else {
			return (float) Math.acos(dot);		// TODO
		}
	}

	/**
	 * Calculates the <i>cross product</i> of this and the given vector.
	 * <p>
	 * The result of the cross product is perpendicular to the given vectors and is thus a normal to the plane containing both.
	 * <p>
	 * Mathematically the cross product is expressed as follows:
	 * <p>
	 * <pre>A x B = |A| |B| sin(angle) N</pre>
	 * <p>
	 * where:
	 * <ul>
	 * <li><i>angle</i> is the angle between the vectors in the plane containing A and B</li>
	 * <li><i>N</i> is a unit-vector perpendicular to the plane (see below)</li>
	 * </ul>
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>the cross product is non-commutative</li>
	 * <li>by convention the direction of the resultant vector is determined by the <i>right-hand rule</i></li>
	 * </ul>
	 * <p>
	 * @param that Vector
	 * @return Cross product
	 * @see <a href="https://en.wikipedia.org/wiki/Cross_product">Wikipedia</a>
	 */
	public Vector cross(Vector that) {
		final float x = this.y * that.z - this.z * that.y;
		final float y = this.z * that.x - this.x * that.z;
		final float z = this.x * that.y - this.y * that.x;
		return new Vector(x, y, z);
	}

	/**
	 * Determines the nearest point on this vector to the given point.
	 * @param p Point
	 * @return Nearest point on this vector
	 */
	public final Point nearest(Point p) {
		final Vector v = new Vector(p);
		final Vector n = v.project(new Normal(this));
		return new Point(n);
	}
	// TODO - specialise in Normal?

	/**
	 * Projects this vector onto the given normal.
	 * <p>
	 * The vector projection of a vector V onto U is: {@code projU(V) = (V.N) N / |N * N|} where:
	 * <ul>
	 * <li>V is <b>this</b> vector</li>
	 * <li>N is the normal to be projected onto</li>
	 * </ul>
	 * Note that the magnitude of N squared is always {@code one} in this implementation.
	 * <p>
	 * @param normal Vector to project onto
	 * @return Projected vector
	 * @see <a href="https://en.wikipedia.org/wiki/Vector_projection">Wikipedia</a>
	 */
	public final Vector project(Normal normal) {
		return normal.multiply(normal.dot(this));
	}

	/**
	 * Reflects this vector about the given normal.
	 * <p>
	 * The reflection R of vector V onto a surface with normal N is: {@code R = V - 2(V.N) N}
	 * <p>
	 * @param normal Normal
	 * @return Reflected vector
	 */
	public final Vector reflect(Normal normal) {
		final float f = -2f * normal.dot(this);
		return normal.multiply(f).add(this);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Vector that) &&
				super.isEqual(that);
	}
}
