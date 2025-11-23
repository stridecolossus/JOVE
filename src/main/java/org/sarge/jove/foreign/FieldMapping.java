package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;

/**
 * A <i>field mapping</i> marshals a structure field.
 * @author Sarge
 */
class FieldMapping {
	private final VarHandle field;
	private final TransformerAdapter transformer;
	private final FieldMarshal marshal;

	/**
	 * Constructor.
	 * @param field				Structure field
	 * @param transformer		Transformer
	 * @param marshal			Off-heap marshalling
	 */
	@SuppressWarnings("rawtypes")
	public FieldMapping(VarHandle field, Transformer transformer, FieldMarshal marshal) {
		this.field = requireNonNull(field);
		this.transformer = new TransformerAdapter(transformer);
		this.marshal = requireNonNull(marshal);
	}

	/**
	 * Marshals this structure field.
	 * @param structure		Structure
	 * @param address		Off-heap structure
	 * @param allocator		Allocator
	 */
	public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
		final Object value = field.get(structure);
		marshal.marshal(value, transformer.delegate(), address, allocator);
	}

	/**
	 * Unmarshals this field to the given structure.
	 * @implNote Skips final fields
	 * @param address			Off-heap structure
	 * @param structure			Structure instance
	 */
	public void unmarshal(MemorySegment address, NativeStructure structure) {
		if(!field.isAccessModeSupported(AccessMode.SET)) {
			// TODO - can we not just omit this mapping? => factory needs to handle optional case
			// TODO - can the generator even work out whether a field should be final? do we want it to? i.e. get rid of final 'sType'
			return;
		}

		final Object value = marshal.unmarshal(address, transformer);
		field.set(structure, value);
	}

	@Override
	public String toString() {
		return String.format("FieldMapping[field=%s transformer=%s marshal=%s]", field, transformer, marshal);
	}
}
