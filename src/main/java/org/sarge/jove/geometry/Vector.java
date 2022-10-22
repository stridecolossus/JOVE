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
		return new Vector(end.subtract(start));
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
	 * @return Scaled vector
	 */
	public Vector multiply(float f) {
		return new Vector(x * f, y * f, z * f);
	}

	/**
	 * Multiplies this vector <i>component wise</i> by the given vector.
	 * @param vec Vector
	 * @return Multiplied vector
	 */
	public Vector multiply(Vector vec) {
		return new Vector(x * vec.x, y * vec.y, z * vec.z);
	}

	/**
	 * @return Normalized (or unit) vector
	 */
	public Vector normalize() {
		final float len = magnitude();
		if(MathsUtil.isEqual(1, len)) {
			return new NormalizedVector(this);
		}
		else {
			final float f = MathsUtil.inverseRoot(len);
			final Vector vec = multiply(f);
			return new NormalizedVector(vec);
		}
	}

	/**
	 * Calculates the angle between this and the given vector.
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
	public Point nearest(Point p) {
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
	 * <li><i>mag</i> is the magnitude of U squared (and therefore is ignored by this implementation)</li>
	 * </ul>
	 * <p>
	 * @param vec Vector to project onto (assumes normalised)
	 * @return Projected vector
	 * @see <a href="https://en.wikipedia.org/wiki/Vector_projection">Wikipedia</a>
	 */
	public Vector project(Vector vec) {
		final Vector n = vec.normalize();
		return n.multiply(dot(n));
	}

	/**
	 * Reflects this vector about the given normal.
	 * <p>
	 * The reflection R of vector V onto a surface with normal N is:
	 * <pre>R = -2(V.N)N + V</pre>
	 * <p>
	 * @param normal Normal
	 * @return Reflected vector
	 * @see <a href="http://www.3dkingdoms.com/weekly/weekly.php?a=2">Reflection</a>
	 */
	public Vector reflect(Vector normal) {
		final Vector n = normal.normalize();
		final float f = -2f * dot(n);
		return n.multiply(f).add(this);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Vector that) &&
				isEqual(that);
	}
}
