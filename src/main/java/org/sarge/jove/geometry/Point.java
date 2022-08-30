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
	public static final Layout LAYOUT = Layout.floats(SIZE);

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
	 * Adds the given tuple to this point.
	 * @param t Tuple
	 * @return Added point
	 */
	public Point add(Tuple t) {
		return new Point(x + t.x, y + t.y, z + t.z);
	}

	/**
	 * Subtracts the given tuple from this point.
	 * @param t Tuple
	 * @return Subtracted point
	 */
	public Point subtract(Tuple t) {
		return new Point(x - t.x, y - t.y, z - t.z);
	}

	/**
	 * Multiplies this point.
	 * @param f Scalar
	 * @return Multiplied point
	 */
	public Point multiply(float f) {
		return new Point(x * f, y * f, z * f);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Tuple that) &&
				isEqual(that);
	}
}
