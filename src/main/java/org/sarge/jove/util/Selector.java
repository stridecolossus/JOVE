package org.sarge.jove.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A <i>selector</i> is used to choose an optimal value from a list of candidates.
 * @author Sarge
 * @param <T> Result type
 */
public class Selector<T> {
	private final Predicate<T> predicate;
	private final Optional<T> def;

	/**
	 * Constructor.
	 * @param predicate		Predicate for the optimal value
	 * @param def			Optional fallback value
	 */
	public Selector(Predicate<T> predicate, T def) {
		this.predicate = notNull(predicate);
		this.def = Optional.ofNullable(def);
	}

	/**
	 * Selects the optimal value from the given list of candidates.
	 * @param list List of candidates
	 * @return Optimal value or the fallback is not available
	 * @throws NoSuchElementException if the optimal is not present in the candidates and the fallback is empty
	 */
	public T select(List<T> list) {
		return list.stream().filter(predicate).findAny().or(() -> def).orElseThrow();
	}
}
