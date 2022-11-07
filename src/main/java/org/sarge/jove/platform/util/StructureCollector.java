package org.sarge.jove.platform.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

import com.sun.jna.Structure;
import com.sun.jna.Structure.ByReference;

/**
 * Helper for JNA structure arrays.
 * <p>
 * JNA auto-magically marshals structure arrays to/from the native layer.
 * However the array elements <b>must</b> be comprised of a contiguous block of memory allocated via one of the {@link Structure#toArray(int)} methods.
 * In addition the native layer often requires a pointer-to-array type, i.e. the <i>first</i> element of an array.
 * <p>
 * This imposes the following restrictions on how the application makes use of structure arrays:
 * <ol>
 * <li>the array can obviously only be allocated once the size of the resultant data is known</li>
 * <li>the elements of the array must be <i>populated</i> (or filled) rather than mapped individually as is the case for a Java stream</li>
 * </ol>
 * <p>
 * Note that the various transform methods return {@code null} for an empty collection as this is generally the value expected by the native layer for the empty case.
 * <p>
 * Examples:
 * <pre>
 * // Define a population function
 * BiConsumer&lt;SomeData, SomeStructure&gt; populate = (data, struct) -> { ... };
 *
 * // Transform a collection to an array
 * List&lt;SomeData&gt; data = ...
 * SomeStructure[] array = StructureCollector.array(list, new SomeStructure(), populate);
 *
 * // Transform to a pointer-to-array (assumes by-reference)
 * SomeStructure first = StructureCollector.pointer(list, new SomeStructure(), populate);
 * </pre>
 * <p>
 * @author Sarge
 */
public final class StructureCollector {
	private StructureCollector() {
	}

	/**
	 * Transforms the given collection to a contiguous <i>array</i> of JNA structures.
	 * @param <T> Data type
	 * @param <R> Resultant structure type
	 * @param data			Data collection
	 * @param identity		Identity instance
	 * @param populate		Population function
	 * @return Contiguous array or {@code null} if the data is empty
	 */
	public static <T, R extends Structure> R[] array(Collection<T> data, R identity, BiConsumer<T, R> populate) {
		// Check for empty data
		if(data.isEmpty()) {
			return null;
		}

		// Allocate contiguous array
		@SuppressWarnings("unchecked")
		final R[] array = (R[]) identity.toArray(data.size());

		// Populate array (using an iterator since cannot easily convert collection to a generic array)
		final Iterator<T> itr = data.iterator();
		for(final R element : array) {
			populate.accept(itr.next(), element);
		}
		assert !itr.hasNext();

		return array;
	}

	/**
	 * Converts the given collection to a contiguous <i>pointer-to-array</i> referenced by the <b>first</b> element.
	 * Note that the resultant structure <b>must</b> be a JNA {@link ByReference} type.
	 * @param <T> Data type
	 * @param <R> Resultant JNA structure type
	 * @param data			Data collection
	 * @param identity		Identity instance
	 * @param populate		Population function
	 * @return Pointer-to-array or {@code null} if the data is empty
	 */
	public static <T, R extends Structure & ByReference> R pointer(Collection<T> data, R identity, BiConsumer<T, R> populate) {
		// Construct array
		final R[] array = array(data, identity, populate);

		// Convert to pointer-to-array
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
	 * @param identity		Identity instance
	 * @param populate		Population function
	 * @param chars			Collector characteristics
	 * @return Structure collector
	 * @see #array(Collection, Supplier, BiConsumer)
	 */
	public static <T, R extends Structure> Collector<T, ?, R[]> collector(R identity, BiConsumer<T, R> populate, Characteristics... chars) {
		final BinaryOperator<List<T>> combiner = (left, right) -> {
			left.addAll(right);
			return left;
		};
		final Function<List<T>, R[]> finisher = list -> array(list, identity, populate);
		return Collector.of(ArrayList::new, List::add, combiner, finisher, chars);
	}
	// TODO - unused?
}
