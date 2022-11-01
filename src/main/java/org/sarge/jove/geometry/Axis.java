package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;

/**
 * An <i>axis</i> is a vector representing one of the <i>cardinal</i> axes.
 * <p>
 * Notes:
 * <ul>
 * <li>The positive Vulkan Y axis points <b>down</b></li>
 * <li>Negative Z points <b>into</b> the screen.</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public final class Axis extends NormalizedVector {
	/**
	 * Cardinal axes.
	 */
	public static final Axis
			X = new Axis(0),
			Y = new Axis(1),
			Z = new Axis(2);

	/**
	 * Parses a vector from the given string.
	 * <p>
	 * The string is one of:
	 * <ul>
	 * <li>A case-sensitive axis token, e.g. {@code X}</li>
	 * <li>An inverse axis prefixed with the minus sign, e.g. {@code -X}</li>
	 * <li>Otherwise an arbitrary vector</li>
	 * </ul>
	 * @param str String to parse
	 * @return Parsed vector
	 * @throws IllegalArgumentException for an invalid axis token
	 * @throws NumberFormatException for an invalid vector
	 * @see Vector#CONVERTER
	 * @see #of(String)
	 */
	public static Vector parse(String str) {
		if(str.length() > 2) {
			return CONVERTER.apply(str);
		}
		else
		if(str.startsWith("-")) {
			return of(str.substring(1)).invert();
		}
		else {
			return of(str);
		}
	}

	/**
	 * Parses an axis from the given string (case sensitive).
	 * @param axis Axis
	 * @return Axis
	 * @throws IllegalArgumentException if {@link #axis} is not a valid axis
	 */
	public static Vector of(String axis) {
		return switch(axis) {
			case "X" -> X;
			case "Y" -> Y;
			case "Z" -> Z;
			default -> throw new IllegalArgumentException("Invalid axis: " + axis);
		};
	}

	private final int index;
	private final NormalizedVector inv = super.invert();

	/**
	 * Constructor.
	 * @param index Axis index
	 */
	private Axis(int index) {
		super(axis(index));
		this.index = index;
	}

	private static Vector axis(int index) {
		final float[] axis = new float[SIZE];
		axis[index] = 1;
		return new Vector(axis);
	}

	@Override
	public NormalizedVector invert() {
		return inv;
	}

	/**
	 * Creates a counter-clockwise rotation matrix about this axis.
	 * @param angle Rotation angle (radians)
	 * @return Rotation matrix
	 */
	public Matrix rotation(float angle) {
		final var matrix = new Matrix.Builder().identity();
		final float sin = MathsUtil.sin(angle);
		final float cos = MathsUtil.cos(angle);

		switch(index) {
			case 0 -> matrix
					.set(1, 1, cos)
					.set(1, 2, -sin)
					.set(2, 1, sin)
					.set(2, 2, cos);

			case 1 -> matrix
					.set(0, 0, cos)
					.set(0, 2, sin)
					.set(2, 0, -sin)
					.set(2, 2, cos);

			case 2 -> matrix
					.set(0, 0, cos)
					.set(0, 1, -sin)
					.set(1, 0, sin)
					.set(1, 1, cos);
		}

		return matrix.build();
	}

	/**
	 * Selects the cardinal axis corresponding to the <i>minimal component</i> of the given vector.
	 * For example the vector {@code 1, 0, 2} corresponds to the Y axis.
	 * This operation is generally used to determine an arbitrary local coordinate system about a given vector.
	 * @return Cardinal axis
	 */
	public static Axis minimal(Vector vec) {
		if(vec.x < vec.y) {
			return vec.x < vec.z ? X : Z;
		}
		else {
			return vec.y < vec.z ? Y : Z;
		}
	}
}
