package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;

/**
 * An <i>axis</i> is a vector representing one of the cardinal axes.
 * @author Sarge
 */
public final class Axis extends NormalizedVector {
	/**
	 * X-axis vector.
	 */
	public static final Axis X = axis(0);

	/**
	 * Y-axis vector (note Vulkan positive Y axis is <b>down</b>).
	 */
	public static final Axis Y = axis(1);

	/**
	 * Z-axis vector (negative Z is <i>into</i> the screen).
	 */
	public static final Axis Z = axis(2);

	/**
	 * Builds an axis.
	 * @param index Axis index
	 * @return New axis
	 */
	private static Axis axis(int index) {
		final float[] axis = new float[3];
		axis[index] = 1;
		return new Axis(new Vector(axis));
	}

	/**
	 * Constructor.
	 */
	private Axis(Vector axis) {
		super(axis);
	}

	/**
	 * Creates a rotation matrix about this axis.
	 * @param angle Rotation angle (radians)
	 * @return Rotation matrix
	 */
	public Matrix matrix(float angle) {
		final var matrix = new Matrix.Builder().identity();
		final float sin = MathsUtil.sin(angle);
		final float cos = MathsUtil.cos(angle);
		if(this == X) {
			matrix.set(1, 1, cos);
			matrix.set(1, 2, -sin);
			matrix.set(2, 1, sin);
			matrix.set(2, 2, cos);
		}
		else
		if(this == Y) {
			matrix.set(0, 0, cos);
			matrix.set(0, 2, sin);
			matrix.set(2, 0, -sin);
			matrix.set(2, 2, cos);
		}
		else {
			matrix.set(0, 0, cos);
			matrix.set(0, 1, -sin);
			matrix.set(1, 0, sin);
			matrix.set(1, 1, cos);
		}
		return matrix.build();
	}
}
