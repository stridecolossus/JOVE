package org.sarge.jove.geometry;

import java.util.Arrays;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * Volume extents represented by min/max bounds.
 * @author Sarge
 */
public record Extents(Point min, Point max) {
	/**
	 * Empty extents.
	 */
	public static final Extents EMPTY = new Extents(Point.ORIGIN, Point.ORIGIN);

	/**
	 * Constructor.
	 * @param min Minimum extents
	 * @param max Maximum extents
	 */
	public Extents {
		Check.notNull(min);
		Check.notNull(max);
	}

	/**
	 * @return Centre point
	 */
	public Point centre() {
		final Vector vec = Vector.between(min, max);
		return min.add(vec.scale(MathsUtil.HALF));
	}

	/**
	 * @return Largest extent size
	 */
	public float size() {
		final Vector vec = Vector.between(min, max);
		return Float.max(vec.x(), Float.max(vec.y(), vec.z()));
	}

	/**
	 * Builder for extents.
	 */
	public static class Builder {
		private final float[] min, max;

		/**
		 * Constructor.
		 */
		public Builder() {
			min = init(Float.MAX_VALUE);
			max = init(Float.MIN_VALUE);
		}

		/**
		 * Initialises min/max bounds.
		 * @param value Initial value
		 * @return Min/max bounds array
		 */
		private static float[] init(float value) {
			final float[] array = new float[3];
			Arrays.fill(array, value);
			return array;
		}

		/**
		 * Adds a point to this extents.
		 * @param pt Point to add
		 */
		public Builder add(Point pt) {
			update(pt.x(), 0);
			update(pt.y(), 1);
			update(pt.z(), 2);
			return this;
		}

		/**
		 * Updates min/max bounds.
		 * @param value Coordinate
		 * @param index Array index
		 */
		private void update(float value, int index) {
			if(value < min[index]) {
				min[index] = value;
			}
			else
			if(value > max[index]) {
				max[index] = value;
			}
		}

		/**
		 * Constructs extents bounding the added points.
		 * The behaviour of an extents builder without any points is undefined.
		 * @return New extents
		 * @see #add(Point)
		 */
		public Extents build() {
			return new Extents(Point.of(min), Point.of(max));
		}
	}
}
