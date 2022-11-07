package org.sarge.jove.util;

import java.util.*;

import org.sarge.jove.util.FloatSupport.FloatUnaryOperator;

/**
 * A <i>float table</i> is used to improve the performance of mathematical calculations by using a lookup table of approximated values.
 * @author Sarge
 */
@FunctionalInterface
public interface FloatTable extends FloatUnaryOperator {
//	/**
//	 * Looks up the result for the given value.
//	 * @param value Value to lookup
//	 * @return Approximated value
//	 */
//	float get(float value);

//	/**
//	 * Creates a float table given a range and an approximation function.
//	 * @param start		Range start
//	 * @param end		Range end
//	 * @param size		Size of the table
//	 * @param func		Function
//	 * @return New float table
//	 */
//	static FloatTable of(float start, float end, int size, FloatUnaryOperator func) {
//		// Validate
//		if(size <= 0) throw new IllegalArgumentException("Size must be greater than zero");
//		if(start >= end) throw new IllegalArgumentException("Invalid range");
//
//		// Build table
//		final float[] table = new float[size];
//		final float step = (end - start) / size;
//		for(int n = 0; n < size; ++n) {
//			final float a = (n + MathsUtil.HALF) * step;
//			table[n] = func.apply(a);
//		}
//
//		// Create lookup function
//		final float scale = 1 / step;
//		final int mask = size - 1;
//		return angle -> {
//			final int index = (int) (angle * scale) & mask;
//			return table[index];
//		};
//	}

//	https://github.com/AlessandroBorges/IDX3D/blob/idx3d_Java6/source/idx3d/tests/Math2.java
// https://stackoverflow.com/questions/13460693/using-sincos-in-java

	static FloatTable cos() {

		final FloatUnaryOperator cos = angle -> {
			if(MathsUtil.isZero(angle)) {
				return 1;
			}
			else {
				return MathsUtil.cos(angle);
			}
		};

		return new FloatTable.Builder()
				.size(12)
				.end(MathsUtil.TWO_PI)
//				.set(0, 1)
//				.set(MathsUtil.HALF_PI, 0)
//				.set(MathsUtil.PI, -1)
//				.set(MathsUtil.PI + MathsUtil.HALF_PI, 0)
//				.set(MathsUtil.TWO_PI, 1)
				.build(MathsUtil::cos);
	}

//	assertEquals(1, cos.apply(0));
//	assertEquals(0, cos.apply(MathsUtil.HALF_PI));
//	assertEquals(0, cos.apply(MathsUtil.PI));
//	assertEquals(-1, cos.apply(MathsUtil.PI + MathsUtil.HALF_PI));
//	assertEquals(0, cos.apply(MathsUtil.TWO_PI));

	class Builder {
		private final Map<Float, Float> defined = new HashMap<>();
		private float start, end;
		private int size;

		/**
		 * Constructor.
		 */
		public Builder() {
			size(8);
		}

		/**
		 * Sets the start of the range.
		 * @param start Range start
		 */
		public Builder start(float start) {
			this.start = start;
			return this;
		}

		/**
		 * Sets the end of the range.
		 * @param end Range end
		 */
		public Builder end(float end) {
			this.end = end;
			return this;
		}

		/**
		 * Sets the size of the lookup table according to the given number of bits.
		 * The default is 8 bits corresponding to table with 256 entries.
		 * @param size Number of bits
		 * @throws IllegalArgumentException if {@link #size} is less than one
		 */
		public Builder size(int size) {
			if(size < 1) throw new IllegalArgumentException("Size must be greater than zero");
			this.size = 1 << size;
			return this;
		}

		/**
		 * Sets an explicitly defined table entry.
		 * @param value Entry value
		 */
		public Builder set(float key, float value) {
			defined.put(key, value);
			return this;
		}

		/**
		 * Constructs this lookup table.
		 * @param func Approximation function
		 * @return New table
		 * @throws IllegalArgumentException if the range is invalid
		 */
		public FloatTable build(FloatUnaryOperator func) {
			// Validate
			if(start >= end) throw new IllegalArgumentException("Invalid range");

			// Build table
			final float[] table = new float[size];
			final float step = (end - start) / size;
			for(int n = 0; n < size; ++n) {
//				final float f = (n + MathsUtil.HALF) * step;
// TODO - fiddle factor / rounding function
				final float key = n * step;
				Float value = defined.remove(key);
				if(value == null) {
					value = func.apply(key);
				}
				table[n] = value;
			}

			if(!defined.isEmpty()) throw new IllegalStateException("TODO");

			// Create index mapper
			final float scale = 1 / step;
			return value -> {
				// TODO - factor out to helper => can then use mapping for set() override
				final int index = (int) (value * scale) & (size - 1);
				return table[index];
			};
		}
	}
}

//@Test
//void test() {
//	int bits = 12;
//	int mask = ~(-1 << bits);
//	int count = mask + 1;
//
//	float[] array = new float[count];
//	for(int n = 0; n < count; ++n) {
//		array[n] = MathsUtil.cos((n + MathsUtil.HALF) / count * MathsUtil.TWO_PI);
//	}
//	// TODO - cardinal
//
//	float map = count / MathsUtil.TWO_PI;
//	float result = array[(int)(MathsUtil.PI * map) & mask];
//
//}
//}
//
////for (int i = 0; i < SIN_COUNT; i++)
////	table[i] = (float)Math.sin((i + 0.5f) / SIN_COUNT * radFull);
////// The four right angles get extra-precise values, because they are
////// the most likely to need to be correct.
////table[0] = 0f;
////table[(int)(90 * degToIndex) & SIN_MASK] = 1f;
////table[(int)(180 * degToIndex) & SIN_MASK] = 0f;
////table[(int)(270 * degToIndex) & SIN_MASK] = -1f;
