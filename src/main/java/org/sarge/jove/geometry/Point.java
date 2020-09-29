package org.sarge.jove.geometry;

import org.sarge.jove.util.Converter;
import org.sarge.jove.util.JoveUtil;

/**
 * Point in 3D space.
 * @author Sarge
 */
public final class Point extends Tuple {
	/**
	 * Origin point.
	 */
	public static final Point ORIGIN = new Point(0, 0, 0);

	/**
	 * Point converter.
	 */
	public static final Converter<Point> CONVERTER = JoveUtil.converter(3, Point::new);

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
	 * Copy constructor.
	 * @param t Tuple to copy
	 */
	public Point(Tuple t) {
		super(t);
	}

	/**
	 * Adds the given tuple to this point.
	 * @param t Tuple to add
	 * @return New point
	 */
	public Point add(Tuple t) {
		return new Point(x + t.x, y + t.y, z + t.z);
	}

	/**
	 * Calculates the distance (<b>squared</b>) to the given point.
	 * @param point Destination point
	 * @return Distance squared
	 */
	public float distance(Tuple point) {
		final float dx = point.x - x;
		final float dy = point.y - y;
		final float dz = point.z - z;
		return dx * dx + dy * dy + dz * dz;
	}
}
