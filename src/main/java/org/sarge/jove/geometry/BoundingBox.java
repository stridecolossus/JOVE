package org.sarge.jove.geometry;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.Stream;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * Axis-aligned bounding-box volume specified by min/max points.
 * @author Sarge
 */
public class BoundingBox implements BoundingVolume {
	private final Point min, max;
	private final Point centre;

	/**
	 * Constructor given min/max points.
	 * @param min Minimum coordinate
	 * @param max Maximum coordinate
	 */
	public BoundingBox(Point min, Point max) {
		Check.notNull(min);
		Check.notNull(max);
		this.min = min;
		this.max = max;
		this.centre = calculateCentre(min, max);
	}
	
	private static Point calculateCentre(Point min, Point max) {
		final Point extents = max.add(min.scale(-1));
		final Point half = extents.scale(MathsUtil.HALF);
		return min.add(half);
	}
	
	@Override
	public Point getCentre() {
		return centre;
	}

	/**
	 * @return Minimum point of this box
	 */
	public Point getMinimum() {
		return min;
	}

	/**
	 * @return Minimum point of this box
	 */
	public Point getMaximum() {
		return max;
	}

	/**
	 * @param pt Point to test
	 * @return Whether this bounding box contains the given point
	 */
	@Override
	public boolean contains(Point pt) {
		if(!contains(pt.x, min.x, max.x)) return false;
		if(!contains(pt.y, min.y, max.y)) return false;
		if(!contains(pt.z, min.z, max.z)) return false;
		return true;
	}

	private static boolean contains(float value, float min, float max) {
		return (value >= min) && (value <= max);
	}

	@Override
	public boolean intersects(Ray ray) {
		// TODO
		return false;
	}

	@Override
	public String toString() {
		return min + "/" + max;
	}
	
	/**
	 * Bounding box collector.
	 */
	private static class BoundsCollector {
		/**
		 * Initialises a min/max working array.
		 * @param value Initial value
		 * @return Array
		 */
		private static double[] init(double value) {
			final double[] array = new double[3];
			Arrays.fill(array, value);
			return array;
		}

		private final double[] min = init(Double.MAX_VALUE);
		private final double[] max = init(Double.MIN_VALUE);
		private final double[] array = new double[3];

		/**
		 * Updates this set of bounds with the given point.
		 * @param pos Point
		 */
		public void accept(Point pos) {
			array[0] = pos.x;
			array[1] = pos.y;
			array[2] = pos.z;
			merge(array, Math::min, min);
			merge(array, Math::max, max);
		}
		
		/**
		 * Merges min/max bounds.
		 * @param accum		Min/max accumulator array
		 * @param array		Array to merge
		 * @param op		Operator
		 */
		private static void merge(double[] array, DoubleBinaryOperator op, double[] accum) {
			for(int n = 0; n < accum.length; ++n) {
				accum[n] = op.applyAsDouble(accum[n], array[n]);
			}
		}
		
		/**
		 * Combine the given bounds with this bounds.
		 * @param other Bounds to combine
		 */
		public void combine(BoundsCollector other) {
			merge(other.min, Math::min, min);
			merge(other.max, Math::max, max);
		}

		/**
		 * Helper: Converts a bounds array to a min/max point.
		 * @param array Min/max array
		 * @return Point
		 */
		public static Point toFloatArray(double[] array) {
			final float[] out = new float[array.length];
			for(int n = 0; n < array.length; ++n) out[n] = (float) array[n];
			return new Point(out);
		}
	}

	/**
	 * Builds a bounding box from the given stream of points.
	 * @param points Stream of points
	 * @return Bounding box
	 */
	public static BoundingBox of(Stream<Point> points) {
		final BoundsCollector stats = points.collect(BoundsCollector::new, BoundsCollector::accept, BoundsCollector::combine);
		final Point min = BoundsCollector.toFloatArray(stats.min);
		final Point max = BoundsCollector.toFloatArray(stats.max);
		return new BoundingBox(min, max);
	}
}
