package org.sarge.jove.util;

/**
 * A <i>cosine table</i> implements the trigonometric functions via a lookup table.
 * <p>
 * Notes:
 * <ul>
 * <li>This implementation sacrifices accuracy for improved performance (of the order of x20)</li>
 * <li>For small angles (less than 45 degrees) the Java maths functions are generally equivalent in terms of performance (and are obviously more accurate)</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public class CosineTable implements Cosine {
	private static final int SIGN = ~1;

	private final float[] table;
	private final float scale;
	private final int mask;

	/**
	 * Constructor.
	 * @param size Table size
	 * @throws IllegalArgumentException if the given size is not a power-of-two
	 */
	public CosineTable(int size) {
		// Init angle scalar
		if(!MathsUtil.isPowerOfTwo(size)) throw new IllegalArgumentException("Cosine table size must be a power-of-two");
		final float step = TWO_PI / size;
		this.scale = 1 / step;
		this.mask = SIGN & (size - 1);

		// Build table
		this.table = new float[size];
		for(int n = 0; n < size; ++n) {
			table[n] = (float) Math.cos(n * step);
		}

		// Explicitly populate the cardinal axes
		init(HALF_PI, 0);
		init(PI, -1);
		init(PI + HALF_PI, 0);
		init(TWO_PI, 1);
	}

	private void init(float angle, float cos) {
		final int index = index(angle);
		table[index] = cos;
	}

	/**
	 * Maps the given angle to the corresponding table index.
	 * @param angle Angle (radians)
	 * @return Table index for the given angle
	 */
	private int index(float angle) {
		final float segment = MathsUtil.HALF + angle * scale;
		return (int) segment & mask;
	}

	@Override
	public float cos(float angle) {
		final int index = index(angle);
		return table[index];
	}
}
