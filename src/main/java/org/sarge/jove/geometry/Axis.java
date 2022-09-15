package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Converter;

/**
 * An <i>axis</i> is a vector representing one of the <i>cardinal</i> axes.
 * @author Sarge
 */
public abstract class Axis extends NormalizedVector {
	/**
	 * Horizontal or <i>right</i> axis.
	 */
	public static final Axis X = new Axis(0) {
		@Override
		protected void rotation(float sin, float cos, Builder matrix) {
			matrix.set(1, 1, cos);
			matrix.set(1, 2, -sin);
			matrix.set(2, 1, sin);
			matrix.set(2, 2, cos);
		}
	};

	/**
	 * Vulkan positive Y axis is <b>down</b>.
	 */
	public static final Axis Y = new Axis(1) {
		@Override
		protected void rotation(float sin, float cos, Builder matrix) {
			matrix.set(0, 0, cos);
			matrix.set(0, 2, sin);
			matrix.set(2, 0, -sin);
			matrix.set(2, 2, cos);
		}
	};

	/**
	 * Negative Z is <b>into</b> the screen.
	 */
	public static final Axis Z = new Axis(2) {
		@Override
		protected void rotation(float sin, float cos, Builder matrix) {
			matrix.set(0, 0, cos);
			matrix.set(0, 1, -sin);
			matrix.set(1, 0, sin);
			matrix.set(1, 1, cos);
		}
	};

	/**
	 * Axis/vector converter.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>Converts axis tokens, e.g. {@code X} maps to {@link #X}</li>
	 * <li>Also handles inverse axes prefixed with the negative symbol, e.g. {@code -X}</li>
	 * <li>Otherwise the string is assumed to be an arbitrary vector, i.e. delegates to {@link Vector#CONVERTER}</li>
	 * </ul>
	 */
	@SuppressWarnings("hiding")
	public static final Converter<Vector> CONVERTER = new Converter<>() {
		@Override
		public Vector apply(String str) throws NumberFormatException {
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
	};

	/**
	 * Converts the given string to an axis.
	 * @param axis Axis name
	 * @return Axis
	 * @throws IllegalArgumentException for an invalid axis
	 */
	public static Axis of(String axis) {
		return switch(axis) {
			case "X" -> Axis.X;
			case "Y" -> Axis.Y;
			case "Z" -> Axis.Z;
			default -> throw new IllegalArgumentException("Invalid axis: " + axis);
		};
	}

	private final Vector inv = super.invert();

	/**
	 * Constructor.
	 */
	private Axis(int index) {
		super(axis(index));
	}

	/**
	 * Builds the vector for an axis.
	 * @param axis Axis index
	 * @return New axis
	 */
	private static Vector axis(int axis) {
		final float[] array = new float[3];
		array[axis] = 1;
		return new Vector(array);
	}

	@Override
	public Vector invert() {
		return inv;
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
	 * For example the vector {@code 0, 1, 2} corresponds to the X axis.
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
