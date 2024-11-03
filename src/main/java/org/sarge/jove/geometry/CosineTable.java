package org.sarge.jove.geometry;

import static org.sarge.jove.util.MathsUtility.*;

import java.util.Arrays;

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
public class CosineTable implements Cosine.Provider {
	private final Cosine[] table;
	private final float scale;

	/**
	 * Constructor.
	 * @param size Table size
	 * @throws IllegalArgumentException if the given size is not a power-of-two
	 */
	public CosineTable(int size) {
		this(size, Cosine.Provider.DEFAULT);
	}

	/**
	 * Constructor given a custom cosine function.
	 * @param size 			Table size
	 * @param provider		Cosine function
	 * @throws IllegalArgumentException if {@link #size} is not four or more
	 */
	public CosineTable(int size, Cosine.Provider provider) {
		if(size < 4) throw new IllegalArgumentException();
		final float segment = TWO_PI / size;
		this.scale = 1 / segment;
		this.table = new Cosine[size];
		Arrays.setAll(table, index -> provider.cosine(index * segment));
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
	public Cosine cosine(float angle) {
		return table[index(angle)];
	}
}
