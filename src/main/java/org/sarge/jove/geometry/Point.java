package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Converter;


/**
 * Point in 3D space.
 * @author Sarge
 */
public final class Point extends Tuple {
	/**
	 * Origin.
	 */
	public static final Point ORIGIN = new Point();
	
	/**
	 * String-to-point converter.
	 */
	public static final Converter<Point> CONVERTER = str -> new Point(MathsUtil.convert(str, SIZE));

	/**
	 * Origin constructor.
	 */
	public Point() {
		super();
	}

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param z
	 */
	public Point(float x, float y, float z) {
		super(x, y, z);
	}

	/**
	 * Array constructor.
	 * @param array Point array
	 */
	public Point(float[] array) {
		super(array);
	}

	/**
	 * @param pt Point
	 * @return Distance squared to the given point
	 */
	public float distanceSquared(Point pt) {
		final float dx = pt.x - x;
		final float dy = pt.y - y;
		final float dz = pt.z - z;
		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * Adds to this point.
	 * @param pt Point to add
	 * @return New point
	 */
	public Point add(Tuple t) {
		return new Point(
			this.x + t.x,
			this.y + t.y,
			this.z + t.z
		);
	}

	/**
	 * Scales this point.
	 * @param scale Scalar
	 * @return Scaled point
	 */
	public Point scale(float scale) {
		return new Point(
			this.x * scale,
			this.y * scale,
			this.z * scale
		);
	}

	/**
	 * Projects this point onto the given vector.
	 * @param vec Vector to project onto (assumes normalised)
	 * @return Projected point
	 */
	public Point project(Vector vec) {
		return scale(vec.dot(this));
	}
}
