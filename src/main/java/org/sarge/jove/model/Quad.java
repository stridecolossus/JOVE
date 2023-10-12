package org.sarge.jove.model;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

/**
 * A <i>quad</i> is a rectangular area in 3D space.
 * @author Sarge
 */
public record Quad(Point topLeft, Point bottomRight) {
	/**
	 * Constructor.
	 * @param topLeft
	 * @param bottomRight
	 */
	public Quad {
		Check.notNull(topLeft);
		Check.notNull(bottomRight);
	}

	public static Quad of(Point centre, float width, float height, Normal normal) {
		return null;
	}

	public Point centre() {
		return topLeft.add(bottomRight).multiply(MathsUtil.HALF);
	}

	public Normal normal() {
		return null;
	}

//	/**
//	 *
//	 * @return
//	 */
//	public List<Point> triangles() {
//		return null;
//	}
//
//	/**
//	 * Generates a strip of quads vertices
//	 * @param quads
//	 * @return
//	 */
//	public List<Point> strip(int quads) {
//		return null;
//	}

	/**
	 * Rotates the vertices of this quad.
	 * @param rotation Rotation
	 * @return Rotated quad
	 */
	public Quad rotate(Rotation rotation) {
		return null;
	}

	/**
	 * Flips this quad about the given about the given axis, i.e. rotates by 180 degrees.
	 * @param axis Flip axis
	 * @return Flipped quad
	 * @see #rotate(Rotation)
	 */
	public Quad flip(Normal axis) {
		return rotate(new AxisAngle(axis, Trigonometric.PI));
	}
}
