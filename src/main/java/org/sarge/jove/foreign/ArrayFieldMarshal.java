package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireZeroOrMore;

import java.lang.foreign.MemorySegment;
import java.lang.reflect.Array;

/**
 * An <i>array field marshal</i> marshals an array field to/from off-heap memory.
 * The {@link #unmarshal(MemorySegment, Transformer)} method instantiates a new array to be populated by the transformer.
 * @author Sarge
 */
class ArrayFieldMarshal extends SliceFieldMarshal {
	private final Class<?> component;
	private final int length;

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

		// Unmarshal off-heap array
		final MemorySegment slice = super.slice(address);
		transformer.update().accept(slice, array);

		return array;
	}

	@Override
	public String toString() {
		return String.format("ArrayFieldMarshal[length=%d component=%s]", component, length);
	}
}
