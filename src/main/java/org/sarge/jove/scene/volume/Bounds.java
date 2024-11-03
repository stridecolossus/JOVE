package org.sarge.jove.scene.volume;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>bounds</i> defines an axis-aligned rectilinear volume specified by min/max points.
 * @see BoundingBox
 * @author Sarge
 */
public record Bounds(Point min, Point max) {
	/**
	 * Empty bounds.
	 */
	public static final Bounds EMPTY = new Bounds(Point.ORIGIN, Point.ORIGIN);

	/**
	 * Constructor.
	 */
	public Bounds {
		requireNonNull(min);
		requireNonNull(max);
	}

	/**
	 * @return Centre point of this bounds
	 */
	public Point centre() {
		final Vector v = Vector.between(min, max);
		return min.add(v.multiply(MathsUtility.HALF));
	}

	/**
	 * @return Largest extent of this bounds
	 */
	public float largest() {
		final Vector vec = Vector.between(min, max);
		return Math.max(vec.x, Math.max(vec.y, vec.z));
	}

	/**
	 * Tests whether these bounds contain the given point.
	 * @param p Point
	 * @return Whether contained
	 */
	public boolean contains(Point p) {
		return
				contains(p.x, min.x, max.y) &&
				contains(p.y, min.y, max.y) &&
				contains(p.z, min.z, max.z);
	}

	private static boolean contains(float f, float min, float max) {
		return (f >= min) && (f <= max);
	}

	/**
	 * Determines the vertex of these bounds nearest to the given point.
	 * @param p Point
	 * @return Nearest point
	 */
	public Point nearest(Point p) {
		final float[] point = p.toArray();
		final float[] a = min.toArray();
		final float[] b = max.toArray();
		final float[] nearest = new float[Point.SIZE];
		for(int n = 0; n < Point.SIZE; ++n) {
			nearest[n] = Math.max(a[n], Math.min(point[n], b[n]));
		}
		return new Point(nearest);
	}

	/**
	 * Calculates the <i>positive</i> vertex of this bounds, i.e. the <b>furthest</b> vertex in the direction of the given vector.
	 * @param vector Vector
	 * @return Positive vertex
	 */
	public Point positive(Vector vector) {
		return new Point(
				vector.x < 0 ? min.x : max.x,
				vector.y < 0 ? min.y : max.y,
				vector.z < 0 ? min.z : max.z
		);
	}

	/**
	 * Calculates the <i>negative</i> vertex of this bounds, i.e. the <b>nearest</b> vertex in the direction of the given vector.
	 * @param vector Vector
	 * @return Negative vertex
	 */
	public Point negative(Vector vector) {
		return new Point(
				vector.x > 0 ? min.x : max.x,
				vector.y > 0 ? min.y : max.y,
				vector.z > 0 ? min.z : max.z
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
		 * @param p Point to add
		 */
		public Builder add(Point p) {
			final float[] array = p.toArray();
			for(int n = 0; n < Point.SIZE; ++n) {
				min[n] = Math.min(array[n], min[n]);
				max[n] = Math.max(array[n], max[n]);
			}
			return this;
		}

		/**
		 * Sums this and the given set of bounds, i.e. computes the aggregate enclosing both.
		 * @param that Bounds to add
		 * @return Summed bounds
		 */
		public Builder sum(Builder that) {
			final Bounds bounds = that.build();
			add(bounds.min);
			add(bounds.max);
			return this;
		}

		/**
		 * Constructs this bounds.
		 * @return New bounds
		 */
		public Bounds build() {
			final Point a = new Point(min);
			final Point b = new Point(max);
			return new Bounds(a, b);
		}

		/**
		 * Creates a collector for computing bounds.
		 * @return Bounds collector
		 */
		public static Collector<Point, ?, Builder> collector() {
			return Collector.of(
					Bounds.Builder::new,
					Bounds.Builder::add,
					Bounds.Builder::sum,
					Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH
			);
		}
	}
}
