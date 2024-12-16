package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.function.*;

/**
 * An <i>array return value</i> is a helper for the special case of an array returned from a native method.
 * <p>
 * This is a special case since the length of the array is unknown during marshalling, therefore such return values cannot be modelled as a Java array.
 * Usually the length is either a known constant or returned separately, often as an integer-by-reference parameter in the same native method.
 * In either case the {@link #array(int, IntFunction)} method extracts the actual array once the length is available.
 * <p>
 * @param <T> Component type
 * @see NativeReference
 * @author Sarge
 */
public class ArrayReturnValue<T> {
	private final MemorySegment address;
	private final TransformerRegistry registry;

	/**
	 * Constructor.
	 * @param address 		Array address
	 * @param registry		Native transformers
	 */
	ArrayReturnValue(MemorySegment address, TransformerRegistry registry) {
		this.address = requireNonNull(address);
		this.registry = requireNonNull(registry);
	}

	/**
	 * Extracts the array given the length.
	 * @param <T> Component type
	 * @param length Array length
	 * @return Array
	 */
	@SuppressWarnings("unchecked")
	public T[] array(int length, IntFunction<T[]> factory) {
		// Allocate array
		final T[] array = factory.apply(length);

		// Lookup element transformer
		@SuppressWarnings("rawtypes")
		final Class type = array.getClass().getComponentType();
		final NativeTransformer<T, MemorySegment> transformer = registry.get(type);

		// Resize to array of pointers
		// TODO - primitives, structures?
		final long size = transformer.layout().byteSize();
		final MemorySegment segment = address.reinterpret(size * length);

		// Populate array
		final Function<MemorySegment, T> mapper = transformer.returns();
		for(int n = 0; n < length; ++n) {
			final MemorySegment element = segment.getAtIndex(ValueLayout.ADDRESS, n);
			// TODO - slice for structures => String[] requires dereference but structures use slice WTF?
			//final MemorySegment element = segment.asSlice(n * size, size).get(ValueLayout.ADDRESS, 0L);
			array[n] = mapper.apply(element);
		}

		return array;
	}
}
