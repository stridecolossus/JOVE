package org.sarge.jove.util;

/**
 * Cosine function implemented via a lookup table.
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

	// https://stackoverflow.com/questions/13460693/using-sincos-in-java
	// https://github.com/AlessandroBorges/IDX3D/blob/idx3d_Java6/source/idx3d/tests/Math2.java

	/**
	 * @return Table index for the given angle
	 */
	private int index(float angle) {
		final float segment = MathsUtil.HALF + angle * scale;
		return (int) segment & mask; //SIGN & (table.length - 1);
	}

	@Override
	public float cos(float angle) {
		final int index = index(angle);
		return table[index];
	}
}
