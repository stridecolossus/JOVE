package org.sarge.jove.foreign;

import java.lang.foreign.*;

/**
 * A <i>field mapping</i> implements the marshalling process of a structure field.
 * @author Sarge
 */
interface FieldMapping<T> {
	/**
	 * Marshals the given value to off-heap memory.
	 * @param value			Value to marshal
	 * @param address		Memory address
	 * @param allocator		Allocator
	 */
	void marshal(T value, MemorySegment address, SegmentAllocator allocator);

	/**
	 * Unmarshals a value from off-heap memory to a native structure.
	 * @param address		Memory address
	 * @param structure		Structure
	 * @return Value
	 */
	Object unmarshal(MemorySegment address, NativeStructure structure);
}
