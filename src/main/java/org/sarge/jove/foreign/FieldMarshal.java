package org.sarge.jove.foreign;

import java.lang.foreign.*;

/**
 * A <i>field marshal</i> is responsible for marshalling to/from off-heap memory.
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
interface FieldMarshal {
	/**
	 * Marshals a structure field to off-heap memory.
	 * @param value				Field value
	 * @param transformer		Transformer
	 * @param address			Off-heap structure
	 * @param allocator			Allocator
	 */
	void marshal(Object value, Transformer transformer, MemorySegment address, SegmentAllocator allocator);

	/**
	 * Unmarshals a structure field from off-heap memory.
	 * @param address			Off-heap structure
	 * @param transformer		Transformer
	 * @return Unmarshalled field value
	 */
	Object unmarshal(MemorySegment address, Transformer transformer);
}
