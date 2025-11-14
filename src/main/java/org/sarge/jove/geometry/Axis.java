package org.sarge.jove.geometry;

/**
 * An <i>axis</i> is one of the three <i>cardinal</i> vector directions.
 * <p>
 * Notes:
 * <ul>
 * <li>The positive Vulkan Y axis points <b>down</b></li>
 * <li>Negative Z points <b>into</b> the screen</li>
 * <li>The {@link #rotation(float, Cosine)} implementation uses a custom implementation TODO</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public class Axis extends Normal {
	/**
	 * Cardinal axes.
	 */
	public static final Axis
        	X = new Axis(0),
           	Y = new Axis(1),
           	Z = new Axis(2);

	private final int ordinal;
	private final Normal invert;

	/**
	 * Constructor.
	 * @param ordinal Axis ordinal
	 */
	private Axis(int ordinal) {
		final Vector vector = vector(ordinal);
		super(vector);
		this.ordinal = ordinal;
		this.invert = new Normal(vector.invert());
	}

	private static Vector vector(int ordinal) {
		final var array = new float[Vector.SIZE];
		array[ordinal] = 1;
		return new Vector(array);
	}

	@Override
	public Normal invert() {
		return invert;
	}

	/**
	 * Creates a transform matrix for a rotation about this axis.
	 * @param angle			Rotation angle (radians, counter-clockwise)
	 * @param provider		Cosine provider
	 * @return Rotation matrix
	 */
	public Matrix rotation(float angle, Cosine.Provider provider) {
		// Init matrix
		final var builder = new Matrix.Builder(4).identity();
		final Cosine cosine = provider.cosine(angle);
		final float sin = cosine.sin();
		final float cos = cosine.cos();

		// Build rotation matrix for this axis
		switch(ordinal) {
			case 0 -> builder
				.set(1, 1, cos)
				.set(1, 2, -sin)
				.set(2, 1, sin)
				.set(2, 2, cos);

			case 1 -> builder
    			.set(0, 0, cos)
    			.set(0, 2, sin)
    			.set(2, 0, -sin)
    			.set(2, 2, cos);

			case 2 -> builder
    			.set(0, 0, cos)
    			.set(0, 1, -sin)
    			.set(1, 0, sin)
    			.set(1, 1, cos);
		}

		return builder.build();
	}

	/**
	 * Parses an axis from the given character.
	 * @param axis Axis character (case insensitive)
	 * @return Cardinal axis
	 */
	public static Axis parse(char axis) {
		return switch(Character.toUpperCase(axis)) {
			case 'X' -> X;
			case 'Y' -> Y;
			case 'Z' -> Z;
			default -> throw new NumberFormatException("Unknown cardinal axis: " + axis);
		};
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
}
