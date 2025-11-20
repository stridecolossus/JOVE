package org.sarge.jove.util;

/**
 * A <i>percentile</i> represents a percentage expressed as a floating-point value in the range {@code 0..1}.
 * @author Sarge
 */
public record Percentile(float value) implements Comparable<Percentile> {
	/**
	 * Maximum value of a percentile expressed as an integer.
	 */
	public static final int MAX_VALUE = 100;

	/**
	 * Zero percentile.
	 */
	public static final Percentile ZERO = new Percentile(0f);

	/**
	 * 50% percentile.
	 */
	public static final Percentile HALF = new Percentile(0.5f);

	/**
	 * 100% percentile.
	 */
	public static final Percentile ONE = new Percentile(1f);

	/**
	 * Parses a percentile from the given string representation.
	 * <p>
	 * A string containing a decimal point is assumed to be a {@code 0..1} floating point value, otherwise it is treated as a {@code 0..100} integer.
	 * <p>
	 * @param percentile Percentile as a string
	 * @return Percentile
	 * @throws NumberFormatException is the string is not a valid numeric
	 * @throws ArrayIndexOutOfBoundsException if the given value is not a valid integer percentile
	 */
	public static Percentile parse(String percentile) {
		if(percentile.indexOf('.') >= 0) {
			return new Percentile(Float.parseFloat(percentile));
		}
		else {
			return new Percentile(Integer.parseInt(percentile));
		}
	}

	/**
	 * Constructor.
	 * @param value Percentile value
	 * @throws IllegalArgumentException if the value is not a valid percentile
	 */
	public Percentile {
		validate(value);
	}

	/**
	 * Constructor given an integer in the range 0..{@link #MAX_VALUE}.
	 * @param value Integer percentile
	 * @throws IllegalArgumentException if the value is not a valid percentile
	 */
	public Percentile(int value) {
		this(value / (float) MAX_VALUE);
	}

	/**
	 * @throws IllegalArgumentException if {@link #value} is not a valid percentile
	 */
	public static float validate(float value) {
		if((value < 0) || (value > 1)) {
			throw new IllegalArgumentException("Invalid percentile: " + value);
		}
		return value;
	}

	/**
	 * @return This percentile as an integer in the range {@code 0..100}
	 */
	public int toInteger() {
		return (int) (value * MAX_VALUE);
	}

	/**
	 * @return Whether this percentile is equal to zero
	 */
	public boolean isZero() {
		return (this == ZERO) || (Float.floatToIntBits(value) == 0);
	}

	/**
	 * Determines the minimum of two percentiles.
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
	 * Determines the maximum of two percentiles.
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

	/**
	 * Inverts this percentile, i.e. {@code 100% - this}
	 * @return Inverted percentile
	 */
	public Percentile invert() {
		return new Percentile(1 - value);
	}

	@Override
	public int compareTo(Percentile that) {
		return Float.compare(this.value, that.value);
	}

	@Override
	public int hashCode() {
        return Float.floatToIntBits(value);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Percentile that) &&
				(this.hashCode() == that.hashCode());
	}

	@Override
	public String toString() {
		final var str = new StringBuilder();
		str.append(toInteger());
		str.append('%');
		return str.toString();
	}
}
