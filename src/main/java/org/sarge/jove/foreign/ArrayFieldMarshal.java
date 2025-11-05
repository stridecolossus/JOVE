package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireZeroOrMore;

import java.lang.foreign.MemorySegment;
import java.lang.reflect.Array;
import java.util.function.BiConsumer;

/**
 * An <i>array field marshal</i> marshals an array field to/from off-heap memory.
 * The {@link #unmarshal(MemorySegment, Transformer)} method instantiates a new array to be populated by the transformer.
 * @author Sarge
 */
class ArrayFieldMarshal extends SliceFieldMarshal {
	private final Class<?> component;
	private final int length;

	@SuppressWarnings("rawtypes")
	private BiConsumer update;

	/**
	 * Constructor.
	 * @param delegate		Marshal array memory
	 * @param component		Array component
	 * @param length		Array length
	 */
	public ArrayFieldMarshal(long offset, long size, Class<?> component, int length) {
		super(offset, size);
		this.component = requireNonNull(component);
		this.length = requireZeroOrMore(length);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Object unmarshal(MemorySegment address, Transformer transformer) {
		// Instantiate array
		final Object array = Array.newInstance(component, length);

		// Cache update function
		if(update == null) {
			update = transformer.update();
		}

		// Unmarshal off-heap array
		final MemorySegment slice = super.slice(address);
		update.accept(slice, array);

		return array;
	}

	@Override
	public String toString() {
		return String.format("ArrayFieldMarshal[length=%d component=%s delegate=%s]", component, length, super.toString());
	}
}
