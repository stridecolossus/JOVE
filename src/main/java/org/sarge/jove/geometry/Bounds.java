package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.stream.Collector;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>bounds</i> defines an axis-aligned rectilinear volume specified by min/max points.
 * @author Sarge
 */
public class Bounds {
	private final Point min, max;

	/**
	 * Constructor.
	 * @param min Minimum extent
	 * @param max Maximum extent
	 */
	public Bounds(Point min, Point max) {
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
	 * @return Centre point of this bounds
	 */
	public Point centre() {
		return min.add(max).scale(MathsUtil.HALF);
	}

	/**
	 * @return Largest extent size
	 */
	public float largest() {
		final Vector vec = Vector.between(min, max);
		return Math.max(vec.x, Math.max(vec.y, vec.z));
	}

	/**
	 * Tests whether this bounds contains the given point.
	 * @param pt Point
	 * @return Whether contained
	 */
	public boolean contains(Point pt) {
		return
				contains(pt.x, min.x, max.y) &&
				contains(pt.y, min.y, max.y) &&
				contains(pt.z, min.z, max.z);
	}

	private static boolean contains(float f, float min, float max) {
		return (f >= min) && (f <= max);
	}

	/**
	 * Determines the vertex of this bounds nearest to the given point.
	 * @param pt Point
	 * @return Nearest point
	 */
	public Point nearest(Point pt) {
		final float x = nearest(pt.x, min.x, max.x);
		final float y = nearest(pt.y, min.y, max.y);
		final float z = nearest(pt.z, min.z, max.z);
		return new Point(x, y, z);
	}

	private static float nearest(float value, float min, float max) {
		return Math.max(min, Math.min(value, max));
	}

	/**
	 * Calculates the <i>positive</i> vertex of this bounds, i.e. the <b>furthest</b> vertex in the direction of the normal.
	 * @param normal Normal
	 * @return Positive vertex
	 */
	public Point positive(Vector normal) {
		return new Point(
				normal.x < 0 ? min.x : max.x,
				normal.y < 0 ? min.y : max.y,
				normal.z < 0 ? min.z : max.z
		);
	}

	/**
	 * Calculates the <i>negative</i> vertex of this bounds, i.e. the <b>nearest</b> vertex in the direction of the normal.
	 * @param normal Normal
	 * @return Negative vertex
	 */
	public Point negative(Vector normal) {
		return new Point(
				normal.x > 0 ? min.x : max.x,
				normal.y > 0 ? min.y : max.y,
				normal.z > 0 ? min.z : max.z
		);
	}

	/**
	 * Tests whether two bounds intersect.
	 * @param that Bounds
	 * @return Whether the bounds intersect
	 */
	public boolean intersects(Bounds that) {
		return
				(min.x <= that.max.x) && (max.x >= that.min.x) &&
				(min.y <= that.max.y) && (max.y >= that.min.y) &&
				(min.z <= that.max.z) && (max.z >= that.min.z);
	}

	/**
	 * Inverts this bounds.
	 * @return Inverted bounds
	 */
	public Bounds invert() {
		return new Bounds(min, max) {
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
		return
				(obj == this) ||
				(obj instanceof Bounds that) &&
				min.equals(that.min) && max.equals(that.max);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(min).append(max).build();
	}

	/**
	 * Helper - Creates a collector that constructs bounds from a stream of points.
	 * @return New bounds collector
	 */
	public static Collector<Point, ?, Bounds> collector() {
		return Collector.of(Builder::new, Builder::add, Builder::combine, Builder::build);
	}

	/**
	 * Builder for bounds.
	 */
	public static class Builder {
		private final float[] min, max;

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
			final float[] array = new float[Point.SIZE];
			Arrays.fill(array, value);
			return array;
		}

		/**
		 * Adds a point to this bounds.
		 * @param pt Point to add
		 */
		public Builder add(Point pt) {
			for(int n = 0; n < Point.SIZE; ++n) {
				update(pt.get(n), n);
			}
			return this;
		}

		/**
		 * Updates min/max bounds.
		 * @param value Coordinate
		 * @param index Array index
		 */
		private void update(float value, int index) {
			min[index] = Math.min(value, min[index]);
			max[index] = Math.max(value, max[index]);
		}

		/**
		 * Combiner function for this builder when used as a collector.
		 */
		protected static Builder combine(Builder left, Builder right) {
			for(int n = 0; n < Point.SIZE; ++n) {
				left.update(right.min[n], n);
				left.update(right.max[n], n);
			}
			return left;
		}

		/**
		 * Constructs this bounds.
		 * @return New bounds
		 */
		public Bounds build() {
			return new Bounds(new Point(min), new Point(max));
		}
	}
}