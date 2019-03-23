package org.sarge.jove.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility methods for generating a <i>power set</i>.
 * @author Sarge
 */
public final class PowerSet {
	private PowerSet() {
	}

	/**
	 * Generates a power set of bit-fields.
	 * @param size Set size
	 * @return Power set
	 */
	public static IntStream power(int size) {
		final int len = (int) Math.pow(2, size);
		return IntStream.range(0, len);
	}

	/**
	 * Generates the <i>power set</i> of the given collection.
	 * @param c Collection
	 * @return Power set
	 * @param <T> Type
	 */
	public static <T> Stream<Set<T>> power(Collection<T> c) {
		final List<T> list = new ArrayList<>(c);
		final int size = c.size();
		final IntFunction<Set<T>> mapper = mask -> {
			final Set<T> result = new HashSet<>();
			for(int n = 0; n < size; ++n) {
				if((mask & (1 << n)) != 0) {
					result.add(list.get(n));
				}
			}
			return Set.copyOf(result);
		};
		return power(size).mapToObj(mapper);
	}
}
