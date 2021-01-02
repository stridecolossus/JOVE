package org.sarge.jove.common;

import org.sarge.jove.util.Check;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>percentile</i> represents a value in the range 0 to 100%.
 * @author Sarge
 * @see Check#isPercentile(float)
 */
public final class Percentile extends Number implements Comparable<Percentile> {
	/**
	 * Maximum value of a percentile expressed as an integer.
	 */
	public static final int MAX = 100;

	/**
	 * Zero percentile.
	 */
	public static final Percentile ZERO = new Percentile(0);

	/**
	 * 50% percentile.
	 */
	public static final Percentile HALF = new Percentile(0.5f);

	/**
	 * 100% percentile.
	 */
	public static final Percentile ONE = new Percentile(1);

	private static final Percentile[] INTEGERS = new Percentile[MAX + 1];

	static {
		for(int n = 0; n <= MAX; ++n) {
			INTEGERS[n] = new Percentile(n / (float) MAX);
		}
	}

	/**
	 * Creates an integer percentile.
	 * @param value Percentile as a 0..100 integer
	 * @return Percentile
	 * @throws ArrayIndexOutOfBoundsException if the given value is not in the range 0...100
	 */
	public static Percentile of(int value) {
		return INTEGERS[value];
	}

	/**
	 * Parses a percentile from the given string representation.
	 * Assumes the string is either a 0..1 floating-point value or a 0..100 integer.
	 * @param str Percentile as a string
	 * @return Percentile
	 * @see Float#parseFloat(String)
	 * @see Integer#parseInt(String)
	 */
	public static Percentile parse(String str) {
		if(str.indexOf('.') >= 0) {
			return new Percentile(Float.parseFloat(str));
		}
		else {
			return Percentile.of(Integer.parseInt(str));
		}
	}

	private final float value;

	/**
	 * Constructor.
	 * @param value Percentile as a 0..1 floating-point value
	 */
	public Percentile(float value) {
		Check.isPercentile(value);
		this.value = value;
	}

	@Override
	public int intValue() {
		return (int) value;
	}

	@Override
	public long longValue() {
		return (long) value;
	}

	@Override
	public float floatValue() {
		return value;
	}

	@Override
	public double doubleValue() {
		return value;
	}

	@Override
	public int compareTo(Percentile that) {
		if(this.value < that.value) {
			return -1;
		}
		else
		if(this.value > that.value) {
			return +1;
		}
		else {
			return 0;
		}
	}

	/**
	 * Determines a minimum percentile.
	 * @param that Percentile
	 * @return Minimum percentile
	 */
	public Percentile min(Percentile that) {
		if(this.value < that.value) {
			return this;
		}
		else {
			return that;
		}
	}

	/**
	 * Determines a maximum percentile.
	 * @param that Percentile
	 * @return Maximum percentile
	 */
	public Percentile max(Percentile that) {
		if(this.value > that.value) {
			return this;
		}
		else {
			return that;
		}
	}

	@Override
	public int hashCode() {
        return Float.floatToIntBits(value);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Percentile that) && MathsUtil.isEqual(this.value, that.value);
	}

	@Override
	public String toString() {
		return (int) (value * MAX) + "%";
	}
}
