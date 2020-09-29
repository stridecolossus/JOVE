package org.sarge.jove.util;

import static org.sarge.jove.util.Check.notEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An <i>interpolator</i> applies a mathematical function to a given value.
 * @author Sarge
 */
@FunctionalInterface
public interface Interpolator {
	/**
	 * Applies this interpolator to the given value.
	 * @param value Value to be interpolated
	 * @return Interpolated value
	 */
	float interpolate(float value);

	/**
	 * Linear interpolator
	 */
	Interpolator LINEAR = value -> value;

	/**
	 * Sine interpolator.
	 */
	Interpolator SINE = value -> (1 - MathsUtil.cos(value * MathsUtil.PI)) / 2f;

	/**
	 * Creates an adapter for an interpolator that operates over the given range.
	 * @param start				Start value
	 * @param end				End value
	 * @param interpolator		Interpolator function
	 * @return Interpolator
	 */
	static Interpolator range(float start, float end, Interpolator interpolator) {
		final float range = end - start;
		return value -> start + interpolator.interpolate(value) * range;
	}

	/**
	 * A <i>step interpolator</i> maps a value to a banding table.
	 */
	class StepInterpolator implements Interpolator {
		/**
		 * Interpolator entry.
		 */
		public static class Entry {
			private final float step;
			private final float value;

			/**
			 * Constructor.
			 * @param step		Step
			 * @param value		Interpolated value
			 */
			public Entry(float step, float value) {
				this.step = step;
				this.value = value;
			}

			@Override
			public String toString() {
				return step + " -> " + value;
			}
		}

		private final List<Entry> table;

		/**
		 * Constructor.
		 * @param table Banding table
		 * @throws IllegalArgumentException if the table is empty
		 */
		public StepInterpolator(List<Entry> table) {
			this.table = new ArrayList<>(notEmpty(table));
			Collections.sort(this.table, Comparator.comparing(e -> e.step));
		}

		@Override
		public float interpolate(float value) {
			final Entry def = table.get(table.size() - 1);
			final Entry entry = table.stream().filter(e -> e.step >= value).findAny().orElse(def);
			return entry.value;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}
}
