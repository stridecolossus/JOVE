package org.sarge.jove.platform.vulkan.util;

import static org.sarge.jove.util.Check.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.sun.jna.Structure;

/**
 * TODO
 * @author Sarge
 * @param <T> Data type
 * @param <R> Resultant JNA structure type
 */
public class StructureCollector <T, R extends Structure> implements Collector<T, List<T>, R[]> {
	/**
	 * Convenience adapter for a structure collector that returns the <b>first</b> element of the resultant array.
	 * @param <T> Data type
	 * @param <R> Resultant JNA structure type
	 * @param collector Underlying structure collector
	 * @return First element collector
	 */
	public static <T, R extends Structure> Collector<T, List<T>, R> first(StructureCollector<T, R> collector) {
		return Collectors.collectingAndThen(collector, array -> array[0]);
	}

	private final Supplier<R> identity;
	private final BiConsumer<T, R> populate;
	private final Set<Characteristics> chars;

	/**
	 * Constructor.
	 * @param identity		Identity structure
	 * @param populate		Population function
	 * @param chars			Stream characteristics
	 */
	public StructureCollector(Supplier<R> identity, BiConsumer<T, R> populate, Characteristics... chars) {
		this.identity = notNull(identity);
		this.populate = notNull(populate);
		this.chars = Set.copyOf(Arrays.asList(chars));
	}

	@Override
	public Supplier<List<T>> supplier() {
		return ArrayList::new;
	}

	@Override
	public BiConsumer<List<T>, T> accumulator() {
		return List::add;
	}

	@Override
	public BinaryOperator<List<T>> combiner() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
	}

	@Override
	public Function<List<T>, R[]> finisher() {
		return this::finish;
	}

	@SuppressWarnings("unchecked")
	private R[] finish(List<T> list) {
		// Check for empty data
		if(list.isEmpty()) {
			return null;
		}

		// Allocate contiguous array
		final R[] array = (R[]) identity.get().toArray(list.size());

		// Populate array
		final Iterator<T> itr = list.iterator();
		for(final R element : array) {
			populate.accept(itr.next(), element);
		}
		assert !itr.hasNext();

		return array;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return chars;
	}
}
