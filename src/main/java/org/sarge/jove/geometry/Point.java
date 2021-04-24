package org.sarge.jove.geometry;

import java.nio.ByteBuffer;

import org.sarge.jove.common.Component;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>point</i> is a position in 3D space.
 * @author Sarge
 */
public record Point(float x, float y, float z) implements Component {
	/**
	 * Origin point.
	 */
	public static final Point ORIGIN = new Point(0, 0, 0);

	/**
	 * Creates a point from the given array.
	 * @param array Point array
	 * @return New point
	 * @throws IllegalArgumentException if the array is not comprised of three elements
	 */
	public static Point of(float[] array) {
		if(array.length != 3) throw new IllegalArgumentException("Invalid array length: " + array.length);
		final float x = array[0];
		final float y = array[1];
		final float z = array[2];
		return new Point(x, y, z);
	}

	/**
	 * @return This point as a vector relative to the origin
	 */
	public Vector toVector() {
		return new Vector(x, y, z);
	}

	/**
	 * Moves this point by the given vector.
	 * @param vec Vector
	 * @return Moved point
	 */
	public Point add(Vector vec) {
		return new Point(x + vec.x(), y + vec.y(), z + vec.z());
	}

	@Override
	public Layout layout() {
		return Layout.TUPLE;
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		buffer.putFloat(x).putFloat(y).putFloat(z);
	}

	/**
	 * Calculates the distance (<b>squared</b>) to the given point.
	 * @param point Destination point
	 * @return Distance squared
	 */
	public float distance(Point point) {
		final float dx = point.x - x;
		final float dy = point.y - y;
		final float dz = point.z - z;
		return dx * dx + dy * dy + dz * dz;
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj instanceof Point that) &&
				MathsUtil.isEqual(this.x, that.x) &&
				MathsUtil.isEqual(this.y, that.y) &&
				MathsUtil.isEqual(this.z, that.z);
	}
}
