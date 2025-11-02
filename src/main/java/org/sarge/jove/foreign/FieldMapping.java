package org.sarge.jove.foreign;

import java.lang.foreign.*;

/**
 * TODO
 * @author Sarge
 */
interface FieldMapping {
	/**
	 * Marshal this structure field.
	 * @param structure		Structure
	 * @param address		Off-heap memory
	 * @param allocator		Allocator
	 */
	void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator);

	/**
	 * Unmarshals the structure field.
	 * @param address			Off-heap memory
	 * @param structure			Structure
	 */
	void unmarshal(MemorySegment address, NativeStructure structure);
}
