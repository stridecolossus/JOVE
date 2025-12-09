package org.sarge.jove.foreign;

import static org.sarge.jove.util.Validation.*;

import java.lang.foreign.*;

/**
 * A <i>slice field marshal</i> directly accesses an off-heap memory slice to marshal arrays or nested structure fields.
 * @author Sarge
 */
class SliceFieldMarshal implements FieldMarshal {
	private final long offset;
	private final long size;

	/**
	 * Constructor.
	 * @param offset		Field offset
	 * @param size			Field size
	 */
	public SliceFieldMarshal(long offset, long size) {
		this.offset = requireZeroOrMore(offset);
		this.size = requireOneOrMore(size);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void marshal(Object value, Transformer transformer, MemorySegment address, SegmentAllocator allocator) {
		// Skip empty fields
		if(value == null) {
			return;
		}

		// Unmarshal field
		@SuppressWarnings("unchecked")
		final var result = (MemorySegment) transformer.marshal(value, allocator);

		// Copy field
		final MemorySegment slice = slice(address);
		slice.copyFrom(result);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Object unmarshal(MemorySegment address, Transformer transformer) {
		return transformer.unmarshal().apply(slice(address));
	}

	/**
	 * @param address Off-heap structure
	 * @return Slice
	 */
	protected MemorySegment slice(MemorySegment address) {
		return address.asSlice(offset, size);
	}

	@Override
	public String toString() {
		return String.format("SliceFieldMarshal[offset=%d size=%s]", offset, size);
	}
}
