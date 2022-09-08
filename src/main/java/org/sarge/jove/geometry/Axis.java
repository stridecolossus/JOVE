package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.MathsUtil;

/**
 * An <i>axis</i> is a vector representing one of the cardinal axes.
 * @author Sarge
 */
public abstract class Axis extends NormalizedVector {
	/**
	 * X-axis vector.
	 */
	public static final Axis X = new Axis(axis(0)) {
		@Override
		protected void rotation(float sin, float cos, Builder matrix) {
			matrix.set(1, 1, cos);
			matrix.set(1, 2, -sin);
			matrix.set(2, 1, sin);
			matrix.set(2, 2, cos);
		}
	};

	/**
	 * Y-axis vector (note Vulkan positive Y axis is <b>down</b>).
	 */
	public static final Axis Y = new Axis(axis(1)) {
		@Override
		protected void rotation(float sin, float cos, Builder matrix) {
			matrix.set(0, 0, cos);
			matrix.set(0, 2, sin);
			matrix.set(2, 0, -sin);
			matrix.set(2, 2, cos);
		}
	};

	/**
	 * Z-axis vector (negative Z is <i>into</i> the screen).
	 */
	public static final Axis Z = new Axis(axis(2)) {
		@Override
		protected void rotation(float sin, float cos, Builder matrix) {
			matrix.set(0, 0, cos);
			matrix.set(0, 1, -sin);
			matrix.set(1, 0, sin);
			matrix.set(1, 1, cos);
		}
	};

	/**
	 * Builds the vector for an axis.
	 * @param index Axis index
	 * @return New axis
	 */
	private static Vector axis(int index) {
		final float[] axis = new float[3];
		axis[index] = 1;
		return new Vector(axis);
	}

	private final Vector inv;

	/**
	 * Constructor.
	 */
	private Axis(Vector axis) {
		super(axis);
		inv = axis.invert();
	}

	@Override
	public Vector invert() {
		return inv;
	}

	@Override
	public Vector divisor() {
		return this;
	}

	/**
	 * Constructs the rotation matrix for this axis.
	 */
	protected abstract void rotation(float sin, float cos, Matrix.Builder matrix);

	/**
	 * Creates a rotation matrix about this axis.
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
	 * For example the vector {@code 1, 2, 3} corresponds to the X axis.
	 * @return Cardinal axis
	 */
	public static Vector minimal(Vector vec) {
		if(vec.x < vec.y) {
			return vec.x < vec.z ? X : Z;
		}
		else {
			return vec.y < vec.z ? Y : Z;
		}
	}
}
