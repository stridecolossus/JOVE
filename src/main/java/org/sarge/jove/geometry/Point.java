package org.sarge.jove.geometry;

import org.sarge.jove.common.Layout;
import org.sarge.jove.common.Layout.Component;
import org.sarge.jove.util.FloatArrayConverter;
import org.sarge.lib.util.Converter;

/**
 * A <i>point</i> is a position in 3D space.
 * @author Sarge
 */
public final class Point extends Tuple implements Component {
	/**
	 * Origin point.
	 */
	public static final Point ORIGIN = new Point(0, 0, 0);

	/**
	 * Layout for a point.
	 */
	public static final Layout LAYOUT = Layout.floats(SIZE);

	/**
	 * Point converter.
	 */
	public static final Converter<Point> CONVERTER = new FloatArrayConverter<>(Point.SIZE, Point::new);

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
	 * Calculates the distance <b>squared</b> between this and the given point.
	 * @param p Destination point
	 * @return Distance <b>squared</b> to the given point
	 */
	public float distance(Point p) {
		return Vector.between(this, p).magnitude();
	}

	/**
	 * Adds the given tuple to this point.
	 * @param that Tuple to add
	 * @return Added point
	 */
	public Point add(Tuple that) {
		return new Point(x + that.x, y + that.y, z + that.z);
	}

	/**
	 * Multiplies this point by the given scalar.
	 * @param f Scalar
	 * @return Multiplied point
	 */
	public Point multiply(float f) {
		return new Point(x * f, y * f, z * f);
	}

	@Override
	public Layout layout() {
		return LAYOUT;
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Point that) &&
				isEqual(that);
	}
}
