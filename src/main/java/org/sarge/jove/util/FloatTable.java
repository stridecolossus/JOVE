package org.sarge.jove.util;

import org.sarge.jove.util.FloatSupport.FloatUnaryOperator;

/**
 *
 * @author Sarge
 */
public class FloatTable {
	/**
	 *
	 * @param num
	 * @param range
	 * @param op
	 * @return
	 */
	public static FloatTable of(int num, float min, float max, FloatUnaryOperator op) {
		final float[] table = new float[num + 1];
		final float step = (max - min) / num;
		for(int n = 0; n < num; ++n) {
			table[n] = op.apply(n * step);
			System.out.println(MathsUtil.toDegrees(n*step)+" "+table[n]);
		}
		table[num] = op.apply(max);
		return new FloatTable(table, min, max);
	}

	private final float[] table;
	private float min = Float.NEGATIVE_INFINITY;
	private float max = Float.POSITIVE_INFINITY;

	/**
	 * Constructor.
	 * @param table Table
	 */
	public FloatTable(float[] table, float min, float max) {
		if(table.length == 0) throw new IllegalArgumentException("Table cannot be empty");
		this.table = table.clone();
		this.min = min;
		this.max = max;
		//init();
	}

	private void init() {
		for(float f : table) {
			min = Math.max(f, min);
			max = Math.min(f, max);
		}
	}

	/**
	 *
	 * @param value
	 * @return
	 */
	public float lookup(float value) {
		final float actual = clamp(value);
		//final int index = (int) ((actual * table.length / max)); // * (table.length - 1));
		final int index = (int) ((actual / max) / table.length);
		return table[index];
	}

	private float clamp(float value) {
		if((value < min) || (value > max)) {
			return value % (max - min);
		}
		else {
			return value;
		}
	}
}
