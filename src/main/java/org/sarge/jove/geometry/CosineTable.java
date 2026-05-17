package org.sarge.jove.geometry;

import static org.sarge.jove.util.MathsUtility.*;

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
public class CosineTable implements CosineFunction {
	private final float[] table;
	private final float scale;

	/**
	 * Constructor.
	 * @param size Table size
	 * @throws IllegalArgumentException if the given size is not a power-of-two
	 */
	public CosineTable(int size) {
		this(size, CosineFunction.DEFAULT);
	}

	/**
	 * Constructor given a custom cosine function.
	 * @param size 			Table size
	 * @param function		Cosine function
	 * @throws IllegalArgumentException if {@link #size} is not a power-of-two or is too small
	 */
	public CosineTable(int size, CosineFunction function) {
		if(size < 4) {
			throw new IllegalArgumentException();
		}

		final float segment = TWO_PI / size;
		this.scale = 1 / segment;
		this.table = new float[size];

		for(int n = 0; n < size; ++n) {
			table[n] = function.cos(n * segment);
		}
	}

	/**
	 * Maps the given angle to the corresponding table index.
	 * @param angle Angle (radians)
	 * @return Table index for the given angle
	 */
	private int index(float angle) {
		final float segment = angle * scale + HALF;
		final int index = (int) segment % table.length;
		if(index < 0) {
			return index + (table.length - 1);
		}
		else {
			return index;
		}
	}

	@Override
	public float cos(float angle) {
		final int index = index(angle);
		return table[index];
	}
}
