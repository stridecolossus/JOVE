package org.sarge.jove.geometry;

import org.sarge.jove.util.*;
import org.sarge.lib.util.Converter;

/**
 * A <i>vector</i> is a direction in 3D space.
 * @author Sarge
 */
public class Vector extends Tuple {
	/**
	 * Converter for a vector.
	 */
	public static final Converter<Vector> CONVERTER = new FloatArrayConverter<>(SIZE, Vector::new);

	/**
	 * Creates the vector between the given points, i.e. <code>end - start</code>.
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

	/**
	 * @return Magnitude (or length) <b>squared</b> of this vector
	 * @see #dot(Tuple)
	 */
	public float magnitude() {
		return dot(this);
	}

	/**
	 * Calculates the <i>dot</i> product of two vectors.
	 * <p>
	 * The dot product is also known as the <i>inner</i> or <i>scalar</i> vector product.
	 * <p>
	 * The resultant value expresses the angular relationship between two vectors represented mathematically as:
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
	 * <li>is the <i>magnitude</i> of a vector when applied to itself</li>
	 * </ul>
	 * <p>
	 * @param that Tuple
	 * @return Dot product
	 * @see <a href="https://en.wikipedia.org/wiki/Dot_product">Wikipedia</a>
	 */
	public float dot(Tuple that) {
		return x * that.x + y * that.y + z * that.z;
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
	public final Vector add(Vector vec) {
		return new Vector(x + vec.x, y + vec.y, z + vec.z);
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
	 * Multiplies this vector <i>component wise</i> by the given vector.
	 * @param vec Vector
	 * @return Multiplied vector
	 */
	public final Vector multiply(Vector vec) {
		return new Vector(x * vec.x, y * vec.y, z * vec.z);
	}

	/**
	 * @return Normalized (or unit) vector
	 */
	public Normal normalize() {
		return new Normal(this);
	}

	/**
	 * Calculates the angle between this and the given vector.
	 * @param vec Vector
	 * @return Angle between vectors (radians)
	 * @see #dot(Tuple)
	 */
	public final float angle(Vector vec) {
		final float dot = this.dot(vec);
		if(dot < -1) {
			return Trigonometric.PI;
		}
		else
		if(dot > 1) {
			return 0;
		}
		else {
			return (float) Math.acos(dot);
		}
	}
	// TODO - should this actually be MathsUtil.acos()

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
	 * Determines the nearest point on this vector to the given point.
	 * @param p Point
	 * @return Nearest point on this vector
	 */
	public final Point nearest(Point p) {
		final Vector v = new Vector(p);
		final Vector n = v.project(this);
		return new Point(n);
	}

	/**
	 * Projects this vector onto a given vector.
	 * <p>
	 * The vector projection of a vector V onto U is: <pre>projU(V) = (U.V) U / mag</pre>
	 * where:
	 * <ul>
	 * <li>V is <b>this</b> vector</li>
	 * <li>U is assumed to be normalised</li>
	 * <li><i>mag</i> is the magnitude of U squared (and is therefore ignored by this implementation)</li>
	 * </ul>
	 * <p>
	 * @param vec Vector to project onto (assumes normalised)
	 * @return Projected vector
	 * @see <a href="https://en.wikipedia.org/wiki/Vector_projection">Wikipedia</a>
	 */
	public final Vector project(Vector vec) {
		final Normal n = vec.normalize();
		return n.multiply(n.dot(this));
	}

	/**
	 * Reflects this vector about the given normal.
	 * <p>
	 * The reflection R of vector V onto a surface with normal N is: <pre>R = V-2(V.N)N</pre>
	 * <p>
	 * @param normal Normal
	 * @return Reflected vector
	 */
	public final Vector reflect(Normal normal) {
		final float f = -2f * normal.dot(this);
		return normal.multiply(f).add(this);
	}

	@Override
	public final boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Vector that) &&
				isEqual(that);
	}
}
