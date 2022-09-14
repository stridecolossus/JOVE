package org.sarge.jove.geometry;

import org.sarge.jove.common.Component;
import org.sarge.jove.util.FloatSupport.ArrayConverter;
import org.sarge.lib.util.Converter;

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
	public static final Component LAYOUT = Component.floats(SIZE);

	/**
	 * Point converter.
	 */
	public static final Converter<Point> CONVERTER = new ArrayConverter<>(Point.SIZE, Point::new);

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
	 * @param pt Destination point
	 * @return Distance <b>squared</b>
	 */
	public float distance(Point pt) {
		final Point p = subtract(pt);
		return p.dot(p);
	}

	/**
	 * Adds the given tuple to this point.
	 * @param that Tuple
	 * @return Added point
	 */
	public Point add(Tuple that) {
		return new Point(x + that.x, y + that.y, z + that.z);
	}

	/**
	 * Convenience method - Subtracts the given tuple from this point.
	 * @param that Tuple
	 * @return Subtracted point
	 * @see #add(Tuple)
	 */
	public Point subtract(Tuple that) {
		return new Point(x - that.x, y - that.y, z - that.z);
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
