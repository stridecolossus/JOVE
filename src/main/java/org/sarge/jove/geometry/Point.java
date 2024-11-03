package org.sarge.jove.geometry;

import org.sarge.jove.common.Layout;
import org.sarge.jove.common.Layout.Component;

/**
 * A <i>point</i> is a position in 3D space.
 * @author Sarge
 */
public class Point extends Tuple implements Component {
	/**
	 * Origin point.
	 */
	public static final Point ORIGIN = new Point(0, 0, 0);

	/**
	 * Layout for a vertex position.
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
	 * @param array Point as an array
	 * @throws ArrayIndexOutOfBoundsException if the given array does not contain three elements
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
	 * Moves this point by the given vector.
	 * @param vector Vector
	 * @return Moved point
	 */
	public Point add(Vector vector) {
		return new Point(
				x + vector.x,
				y + vector.y,
				z + vector.z
		);
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
				super.isEqual(that);
	}
}
