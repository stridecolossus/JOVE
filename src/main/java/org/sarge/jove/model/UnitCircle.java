package org.sarge.jove.model;

import java.util.*;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>unit circle</i> defines equally spaced points on a circle or segment of a circle.
 * @author Sarge
 */
public final class UnitCircle {
	private UnitCircle() {
	}

	/**
	 * Creates a unit-circle with the given number of points in the X-Y plane.
	 * @param num 		Number of points
	 * @param radius	Radius
	 * @return Unit-circle
	 */
	public static List<Point> circle(int num, float radius) {
		return segment(num, radius, 0, MathsUtil.TWO_PI);
	}

	/**
	 * Creates a unit-circle segment with the given number of points in the X-Y plane.
	 * @param num 		Number of points
	 * @param radius	Radius
	 * @param start		Start angle (radians)
	 * @param end		End angle (radians)
	 * @return Segment
	 */
	public static List<Point> segment(int num, float radius, float start, float end) {
		final List<Point> points = new ArrayList<>(num);
		final float segment = (end - start) / (num - 1);
		for(int n = 0; n < num; ++n) {
			final float angle = start + segment * n;
			final float x = MathsUtil.sin(angle) * radius;
			final float y = MathsUtil.cos(angle) * radius;
			final Point pos = new Point(x, y, 0);
			points.add(pos);
		}
		return points;
	}
}
// TODO - refactor with sphere
// TODO - unused?
