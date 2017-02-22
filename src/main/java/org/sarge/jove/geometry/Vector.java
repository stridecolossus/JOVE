package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Converter;

/**
 * Immutable vector or direction.
 * @author Sarge
 */
public final class Vector extends Tuple {
	/**
	 * X axis.
	 */
	public static final Vector X_AXIS = new Vector(1, 0, 0);

	/**
	 * Y axis.
	 */
	public static final Vector Y_AXIS = new Vector(0, 1, 0);

	/**
	 * Z axis.
	 */
	public static final Vector Z_AXIS = new Vector(0, 0, 1);

	/**
	 * String-to-vector converter.
	 */
	public static final Converter<Vector> CONVERTER = str -> new Vector(MathsUtil.convert(str, SIZE));
	
	/**
	 * Calculates a vector between the given points.
	 * @param start		Start point
	 * @param end		End point
	 * @return Vector
	 */
	public static Vector between(Point start, Point end) {
		return new Vector(
			end.x - start.x,
			end.y - start.y,
			end.z - start.z
		);
	}

	/**
	 * Origin constructor.
	 */
	public Vector() {
		super();
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
	 * @return Magnitude (or <i>length</i>) <b>squared</b> of this vector
	 */
	public final float getMagnitudeSquared() {
		return x * x + y * y + z * z;
	}

	/**
	 * Calculates the angle between this and the given vector (assumes both normalised).
	 * @param vec Vector
	 * @return Angle between vectors (radians)
	 */
	public final float angle(Vector vec) {
		final float dot = dot(vec);
		if(dot < -1) {
			return -1;
		}
		else if(dot > 1) {
			return 1;
		}
		else {
			return MathsUtil.acos(dot);
		}
	}

	/**
	 * @return Normalised vector
	 */
	public Vector normalize() {
		// Calc length
		final float len = getMagnitudeSquared();

		// Skip if already normalised
		if(MathsUtil.isEqual(len, 1)) return this;

		// Normalise
		return multiply(1f / MathsUtil.sqrt(len));
	}

	/**
	 * Adds a vector to this vector.
	 * @param vec Vector to add
	 * @return New vector
	 */
	public Vector add(Vector vec) {
		return new Vector(
			this.x + vec.x,
			this.y + vec.y,
			this.z + vec.z
		);
	}

	/**
	 * Subtracts the given vector from this vector.
	 * @param vec Vector to subtract
	 * @return Subtracted vector
	 */
	public Vector subtract(Vector vec) {
		return add(vec.invert());
	}

	/**
	 * Multiplies this vector.
	 * @param scale Scalar
	 * @return Scaled vector
	 */
	public Vector multiply(float scale) {
		return new Vector(
			this.x * scale,
			this.y * scale,
			this.z * scale
		);
	}

	/**
	 * Computes the cross-product of this and the given vector.
	 * @param vec Vector
	 * @return Cross-product
	 */
	public Vector cross(Vector vec) {
		final float dx = this.y * vec.z - this.z * vec.y;
		final float dy = this.z * vec.x - this.x * vec.z;
		final float dz = this.x * vec.y - this.y * vec.x;
		return new Vector(dx, dy, dz);
	}

	/**
	 * @return Inverted vector
	 */
	public Vector invert() {
		return new Vector(-x, -y, -z);
	}

	/**
	 * Reflects this vector about the given normal.
	 * @param normal Normal
	 * @return This reflected vector
	 */
	public Vector reflect(Vector normal) {
		final float f = this.dot(normal) * -2f;
		return normal.multiply(f).add(this);
	}
}
