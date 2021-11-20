package org.sarge.jove.geometry;

import org.sarge.jove.common.Layout;

/**
 * A <i>point</i> is a position in 3D space.
 * @author Sarge
 */
public final class Point extends Tuple {
	/**
	 * Origin point.
	 */
	public static final Point ORIGIN = new Point(0, 0, 0);

	/**
	 * Layout for a point.
	 */
	public static final Layout LAYOUT = Layout.of(SIZE);

	/**
	 * Constructor.
	 */
	public Point(float x, float y, float z) {
		super(x, y, z);
	}

	/**
	 * Copy constructor.
	 * @param tuple Tuple to copy
	 */
	public Point(Tuple tuple) {
		super(tuple);
	}

	/**
	 * Array constructor.
	 * @param array Point array
	 * @throws IllegalArgumentException if the array is not comprised of three elements
	 */
	public Point(float[] array) {
		super(array);
	}

	/**
	 * Calculates the distance <b>squared</b> to the given point.
	 * @param point Destination point
	 * @return Distance <b>squared</b>
	 */
	public float distance(Point point) {
		final float dx = point.x - x;
		final float dy = point.y - y;
		final float dz = point.z - z;
		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * Moves this point by the given vector.
	 * @param vec Vector
	 * @return Moved point
	 */
	public Point add(Tuple vec) {
		return new Point(x + vec.x, y + vec.y, z + vec.z);
	}

	/**
	 * Scales this point.
	 * @param scale Scale
	 * @return Scaled point
	 */
	public Point scale(float scale) {
		return new Point(x * scale, y * scale, z * scale);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof Point pt) && super.isEqual(pt);
	}
}
