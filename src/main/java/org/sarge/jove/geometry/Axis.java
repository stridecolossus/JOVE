package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.Cosine;

/**
 * An <i>axis</i> is the unit vector for one of the <i>cardinal</i> axes.
 * <p>
 * Notes:
 * <ul>
 * <li>The positive Vulkan Y axis points <b>down</b></li>
 * <li>Negative Z points <b>into</b> the screen.</li>
 * </ul>
 * <p>
 * Implementation note:
 * This class has optimised implementations for commonly used methods such as {@link #dot(Tuple)} or {@link #cross(Vector)}.
 * It is generally recommended that vector operations delegate accordingly, e.g. {@code vec.dot(this)} rather than {@code this.dot(vec)}.
 * <p>
 * @author Sarge
 */
public final class Axis extends Normal {
	/**
	 * Cardinal axes.
	 */
	public static final Axis
			X = new Axis(Instance.X),
			Y = new Axis(Instance.Y),
			Z = new Axis(Instance.Z);

	/**
	 * Parses a vector from the given string.
	 * <p>
	 * The string is one of:
	 * <ul>
	 * <li>A case-sensitive axis token, e.g. {@code X}</li>
	 * <li>An inverse axis prefixed with the minus sign, e.g. {@code -X}</li>
	 * <li>Otherwise an arbitrary vector parsed by {@link Vector#CONVERTER}</li>
	 * </ul>
	 * @param str String to parse
	 * @return Parsed vector
	 * @throws IllegalArgumentException for an invalid axis token
	 * @throws NumberFormatException for an invalid vector
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
	 * @throws IllegalArgumentException if {@link #axis} is not a valid axis token
	 */
	public static Axis of(String axis) {
		return switch(axis) {
			case "X" -> X;
			case "Y" -> Y;
			case "Z" -> Z;
			default -> throw new IllegalArgumentException("Invalid axis: " + axis);
		};
	}

	private final Instance axis;
	private final Normal inv = super.invert();

	/**
	 * Constructor.
	 * @param index Axis index
	 */
	private Axis(Instance axis) {
		super(axis.vector());
		this.axis = axis;
	}

	@Override
	public Normal invert() {
		return inv;
	}

	@Override
	public float dot(Tuple that) {
		return switch(axis) {
			case X -> x * that.x;
			case Y -> y * that.y;
			case Z -> z * that.z;
		};
	}

	@Override
	public Vector cross(Vector vec) {
		return switch(axis) {
    		case X -> new Vector(0, -vec.z, vec.y);
    		case Y -> new Vector(+vec.z, 0, -vec.x);
    		case Z -> new Vector(-vec.y, +vec.x, 0);
    	};
	}

	/**
	 * Creates a counter-clockwise rotation matrix about this axis.
	 * @param angle 		Rotation angle (radians)
	 * @param cosine		Cosine function
	 * @return Rotation matrix
	 */
	public Matrix rotation(float angle, Cosine cosine) {
		final var matrix = new Matrix.Builder().identity();
		final float sin = cosine.sin(angle);
		final float cos = cosine.cos(angle);
		axis.rotation(sin, cos, matrix);
		return matrix.build();
	}

	/**
	 * Selects the cardinal axis corresponding to the <i>minimal component</i> of the given vector.
	 * For example the vector {@code 1,0,2} corresponds to the Y axis.
	 * This operation is generally used to construct an arbitrary local coordinate system about a given vector.
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

	/**
	 * Axis implementation.
	 */
	@SuppressWarnings("hiding")
	private enum Instance {
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
		 * Builds the rotation matrix for this axis.
		 */
		protected abstract void rotation(float sin, float cos, Matrix.Builder matrix);

		/**
		 * @return Axis vector
		 */
		private Vector vector() {
			final float[] vec = new float[SIZE];
			final int index = ordinal();
			vec[index] = 1;
			return new Vector(vec);
		}
	}
}
