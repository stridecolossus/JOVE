package org.sarge.jove.util;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.*;

/**
 * A <i>priority selector</i> chooses from a list of candidate values or falls back to a given default.
 * @param <T> Selected type
 * @author Sarge
 */
public class PrioritySelector<T> {
	/**
	 * Helper.
	 * @param <T> Selected type
	 * @return Fallback function that returns the <b>first</b> candidate value
	 */
	public static <T> Function<List<T>, T> first() {
		return List::getFirst;
	}

	private final Predicate<T> filter;
	private final Function<List<T>, T> fallback;

	/**
	 * Constructor.
	 * @param filter		Filter
	 * @param fallback		Fallback
	 */
	public PrioritySelector(Predicate<T> filter, Function<List<T>, T> fallback) {
		this.filter = requireNonNull(filter);
		this.fallback = requireNonNull(fallback);
	}

	/**
	 * Constructor for a selector with a literal fallback value.
	 * @param filter		Filter
	 * @param fallback		Fallback value
	 */
	public PrioritySelector(Predicate<T> filter, T fallback) {
		this(filter, _ -> fallback);
	}

	/**
	 * Constructor for a selector using the {@link #first()} candidate as a fallback.
	 * @param filter Filter
	 */
	public PrioritySelector(Predicate<T> filter) {
		this(filter, first());
	}

	/**
	 * Selects from the given list of candidates or falls back to the configured default value.
	 * @param candidates Candidates
	 * @return Selected value
	 * @throws NoSuchElementException if {@link #candidates} is empty or the configured fallback returns an empty result
	 */
	public T select(List<T> candidates) {
		return candidates
				.stream()
				.filter(filter)
				.findAny()
				.or(() -> Optional.ofNullable(fallback.apply(candidates)))
				.orElseThrow();
	}
}
