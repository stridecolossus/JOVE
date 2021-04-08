package org.sarge.jove.platform.vulkan.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.sun.jna.Structure;

/**
 * A <i>structure collector</i> is used to convert a stream to a contiguous JNA structure array.
 * <p>
 * Example usage:
 * <p>
 * <pre>
 * Collection&lt;DomainObject&gt; data = ...
 * BiConsumer&lt;DomainObject, SomeStructure&gt; populate = ...
 * SomeStructure[] array = data.stream().collect(new StructureCollector<>(SomeStructure::new, populate));
 * </pre>
 * The {@code toArray()} helper can be used to transform a collection where the target is the <i>first</i> element of the array:
 * <pre>
 * SomeStructure first = StructureCollector.toArray(data, SomeStructure::new, populate);
 * </pre>
 * <p>
 * @param <T> Data type
 * @param <R> Resultant JNA structure type
 * <p>
 * @author Sarge
 */
public class StructureCollector <T, R extends Structure> implements Collector<T, List<T>, R[]> {
	/**
	 * Helper - Converts the given collection to a contiguous array referenced by the <b>first</b> element.
	 * @param <T> Data type
	 * @param <R> Resultant JNA structure type
	 * @param data			Data
	 * @param identity		Identity constructor
	 * @param populate		Population function
	 * @return <b>First</b> element of the array
	 */
	public static <T, R extends Structure> R toPointer(Collection<T> data, Supplier<R> identity, BiConsumer<T, R> populate) {
		final R[] array = toArray(data, identity, populate);

		if(array == null) {
			return null;
		}
		else {
			return array[0];
		}
	}

	/**
	 * Transforms the given data collection to a contiguous array.
	 * @param <T> Data type
	 * @param <R> Resultant JNA structure type
	 * @param data			Data
	 * @param identity		Identity constructor
	 * @param populate		Population function
	 * @return Contiguous array
	 */
	public static <T, R extends Structure> R[] toArray(Collection<T> data, Supplier<R> identity, BiConsumer<T, R> populate) {
		// Check for empty data
		if(data.isEmpty()) {
			return null;
		}

		// Allocate contiguous array
		@SuppressWarnings("unchecked")
		final R[] array = (R[]) identity.get().toArray(data.size());

		// Populate array
		final Iterator<T> itr = data.iterator();
		for(final R element : array) {
			populate.accept(itr.next(), element);
		}
		assert !itr.hasNext();

		return array;
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
		return list -> toArray(list, identity, populate);
	}

	@Override
	public Set<Characteristics> characteristics() {
		return chars;
	}
}
