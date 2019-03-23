package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Volume extents represented by min/max bounds.
 * @author Sarge
 */
public final class Extents extends AbstractEqualsObject {
	/**
	 * Empty extents.
	 */
	public static final Extents EMPTY = new Extents(Point.ORIGIN, Point.ORIGIN);

	private final Point min, max;

	/**
	 * Constructor.
	 * @param min Minimum extents
	 * @param max Maximum extents
	 */
	public Extents(Point min, Point max) {
		this.min = notNull(min);
		this.max = notNull(max);
	}

	/**
	 * @return Minimum extent
	 */
	public Point min() {
		return min;
	}

	/**
	 * @return Maximum extent
	 */
	public Point max() {
		return max;
	}

	/**
	 * @return Centre point
	 */
	public Point centre() {
		final Vector vec = Vector.of(min, max);
		return min.add(vec.scale(MathsUtil.HALF));
	}

	/**
	 * @return Largest extent size
	 */
	public float size() {
		final Vector vec = Vector.of(min, max);
		return Float.max(vec.x, Float.max(vec.y, vec.z));
	}

	/**
	 * Creates a compound extents.
	 * @param extents Extents to add
	 * @return Compound extents
	 */
	public Extents add(Extents extents) {
		return new Builder()
			.add(this.min)
			.add(this.max)
			.add(extents.min)
			.add(extents.max)
			.build();
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
			final float[] array = pt.toArray();
			for(int n = 0; n < 3; ++n) {
				update(array[n], n);
			}
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
			return new Extents(new Point(min), new Point(max));
		}
	}
}
