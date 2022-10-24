package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.MathsUtil;

/**
 * An <i>axis</i> is a vector representing one of the <i>cardinal</i> axes.
 * @author Sarge
 */
public enum Axis {
	/**
	 * Horizontal or <i>right</i> axis.
	 */
	X {
		@Override
		protected void rotation(float sin, float cos, Builder matrix) {
			matrix
					.set(1, 1, cos)
					.set(1, 2, -sin)
					.set(2, 1, sin)
					.set(2, 2, cos);
		}
	},

	/**
	 * The Vulkan positive Y axis is <b>down</b>.
	 */
	Y {
		@Override
		protected void rotation(float sin, float cos, Builder matrix) {
			matrix
					.set(0, 0, cos)
					.set(0, 2, sin)
					.set(2, 0, -sin)
					.set(2, 2, cos);
		}
	},

	/**
	 * Negative Z is <b>into</b> the screen.
	 */
	Z {
		@Override
		protected void rotation(float sin, float cos, Builder matrix) {
			matrix
					.set(0, 0, cos)
					.set(0, 1, -sin)
					.set(1, 0, sin)
					.set(1, 1, cos);
		}
	};

	/**
	 * Parses an axis or vector from the given string.
	 * <p>
	 * The string is one of:
	 * <ul>
	 * <li>An axis token, e.g. {@code X}</li>
	 * <li>An inverse axis prefixed with the minus sign, e.g. {@code -X}</li>
	 * <li>Otherwise an arbitrary vector</li>
	 * </ul>
	 * @param str String to parse
	 * @return Parsed vector
	 * @throws IllegalArgumentException for an invalid axis token
	 * @throws NumberFormatException for an invalid vector
	 * @see Vector#CONVERTER
	 */
	public static Vector parse(String str) {
		if(str.length() > 2) {
			return Vector.CONVERTER.apply(str);
		}
		else
		if(str.startsWith("-")) {
			return of(str.substring(1)).invert();
		}
		else {
			return of(str);
		}
	}

	private static Vector of(String str) {
		final Axis axis = valueOf(str);
		return axis.vec;
	}

	private final Vector vec;

	/**
	 * Constructor.
	 */
	private Axis() {
		this.vec = axis(ordinal());
	}

	/**
	 * Initialises the vector for this axis.
	 */
	private static Vector axis(int index) {
		// Init axis vector as an array
		final float[] axis = new float[Vector.SIZE];
		axis[index] = 1;

		// Create axis vector
		return new NormalizedVector(new Vector(axis)) {
			private final NormalizedVector inv = super.invert();

			@Override
			public NormalizedVector invert() {
				return inv;
			}
		};
	}

	/**
	 * @return Vector for this axis
	 */
	public Vector vector() {
		return vec;
	}

	/**
	 * Constructs the rotation matrix for this axis.
	 */
	protected abstract void rotation(float sin, float cos, Matrix.Builder matrix);

	/**
	 * Creates a counter-clockwise rotation matrix about this axis.
	 * @param angle Rotation angle (radians)
	 * @return Rotation matrix
	 */
	public Matrix rotation(float angle) {
		final var matrix = new Matrix.Builder().identity();
		final float sin = MathsUtil.sin(angle);
		final float cos = MathsUtil.cos(angle);
		rotation(sin, cos, matrix);
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
