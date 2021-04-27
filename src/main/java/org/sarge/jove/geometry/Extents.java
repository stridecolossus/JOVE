package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.MathsUtil;

/**
 * An <i>extents</i> defines an axis-aligned rectangular area of 3D space specified by min/max points.
 * @author Sarge
 */
public class Extents {
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
	 * @return Centre point of this extents
	 */
	public Point centre() {
		final Vector vec = Vector.between(min, max);
		return min.add(vec.scale(MathsUtil.HALF));
	}

	/**
	 * @return Largest extent size
	 */
	public float largest() {
		final Vector vec = Vector.between(min, max);
		return Math.max(vec.x(), Math.max(vec.y(), vec.z()));
	}

	/**
	 * Determines the nearest point of this extents to the given point.
	 * @param pt Point
	 * @return Nearest point
	 */
	public Point nearest(Point pt) {
		final float x = nearest(pt.x(), min.x(), max.x());
		final float y = nearest(pt.y(), min.y(), max.y());
		final float z = nearest(pt.z(), min.z(), max.z());
		return new Point(x, y, z);
	}

	private static float nearest(float value, float min, float max) {
		return Math.max(min, Math.min(value, max));
	}

	/**
	 * Tests whether this extents contains the given point.
	 * @param pt Point
	 * @return Whether contained
	 */
	public boolean contains(Point pt) {
		return
				contains(pt.x(), min.x(), max.y()) &&
				contains(pt.y(), min.y(), max.y()) &&
				contains(pt.z(), min.z(), max.z());
	}

	private static boolean contains(float f, float min, float max) {
		return (f >= min) && (f <= max);
	}

	/**
	 * Tests whether this extents intersects the given extents.
	 * @param extents Extents
	 * @return Whether the extents intersect
	 */
	public boolean intersects(Extents extents) {
		return
				(min.x() <= extents.max.x()) && (max.x() >= extents.min.x()) &&
				(min.y() <= extents.max.y()) && (max.y() >= extents.min.y()) &&
				(min.z() <= extents.max.z()) && (max.z() >= extents.min.z());
	}

	/**
	 * Inverts this extents.
	 * @return Inverted extents
	 */
	public Extents invert() {
		return new Extents(min, max) {
			@Override
			public boolean contains(Point pt) {
				return !super.contains(pt);
			}
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(min, max);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Extents that) && min.equals(that.min) && max.equals(that.max);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(min).append(max).build();
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
