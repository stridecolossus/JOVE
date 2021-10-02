package org.sarge.jove.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

import com.sun.jna.Structure;

/**
 * Helper for JNA structure arrays.
 * <p>
 * JNA auto-magically marshals structure arrays to/from the native layer.
 * However the array elements <b>must</b> be comprised of a contiguous block of memory allocated via one of the {@link Structure#toArray(int)} methods.
 * <p>
 * This imposes the following restrictions on how the application makes use of structure arrays:
 * <ul>
 * <li>the array can obviously only be allocated once the size of the resultant data is known</li>
 * <li>the elements of the array must be <i>populated</i> (or filled) rather than mapped individually as is the case for a Java stream</li>
 * </ul>
 * <p>
 * In addition the native layer often requires a pointer-to-array type, i.e. the <i>first</i> element of an array.
 * <p>
 * This helper provides methods to transform a collection to a contiguous structure array (or the first element of that array)
 * and the convenience {@link #collector(Supplier, BiConsumer, Characteristics...)} where a stream is more appropriate.
 * <p>
 * Note that the various transform methods return {@code null} for an empty collection as this is generally the value expected by the native layer for the empty case.
 * <p>
 * Examples:
 * <pre>
 *  // Define a population function
 *  BiConsumer&lt;SomeData, SomeStructure&gt; populate = (data, struct) -> { ... };
 *
 *  // Transform a collection to an array
 *  List&lt;SomeData&gt; data = ...
 *  SomeStructure[] array = StructureHelper.array(list, SomeStructure::new, populate);
 *
 *  // Transform to the first element of the array
 *  SomeStructure first = StructureHelper.first(list, SomeStructure::new, populate);
 *
 *  // Collect stream to an array
 *  list.stream().collect(StructureHelper.collector(SomeStructure::new, populate));
 * </pre>
 * <p>
 * @author Sarge
 */
public final class StructureHelper {
	private StructureHelper() {
	}

	/**
	 * Transforms the given collection to a contiguous array of JNA structures.
	 * @param <T> Data type
	 * @param <R> Resultant structure type
	 * @param data			Data collection
	 * @param identity		Identity constructor
	 * @param populate		Population function
	 * @return Contiguous array or {@code null} if the data is empty
	 */
	public static <T, R extends Structure> R[] array(Collection<T> data, Supplier<R> identity, BiConsumer<T, R> populate) {
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

	/**
	 * Converts the given collection to a contiguous array referenced by the <b>first</b> element.
	 * @param <T> Data type
	 * @param <R> Resultant JNA structure type
	 * @param data			Data collection
	 * @param identity		Identity constructor
	 * @param populate		Population function
	 * @return <b>First</b> element of the array or {@code null} if the data is empty
	 */
	public static <T, R extends Structure> R first(Collection<T> data, Supplier<R> identity, BiConsumer<T, R> populate) {
		final R[] array = array(data, identity, populate);
		if(array == null) {
			return null;
		}
		else {
			return array[0];
		}
	}

	/**
	 * Helper - Creates a collector that constructs a contiguous array of JNA structures.
	 * @param <T> Data type
	 * @param <R> Resultant structure type
	 * @param identity		Identity constructor
	 * @param populate		Population function
	 * @param chars			Collector characteristics
	 * @return Structure collector
	 * @see #array(Collection, Supplier, BiConsumer)
	 */
	public static <T, R extends Structure> Collector<T, ?, R[]> collector(Supplier<R> identity, BiConsumer<T, R> populate, Characteristics... chars) {
		final BinaryOperator<List<T>> combiner = (left, right) -> {
			left.addAll(right);
			return left;
		};
		final Function<List<T>, R[]> finisher = list -> array(list, identity, populate);
		return Collector.of(ArrayList::new, List::add, combiner, finisher, chars);
	}
}
