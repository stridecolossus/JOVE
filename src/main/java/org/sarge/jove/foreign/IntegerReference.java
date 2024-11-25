package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * An <i>integer reference</i> maps to a native integer-by-reference value.
 * @author Sarge
 */
public class IntegerReference {
	private MemorySegment address;
	private int value;

	/**
	 * @return Integer value
	 */
	public int value() {
		return value;
	}

	/**
	 * Marshals this integer reference.
	 */
	private MemorySegment marshal(SegmentAllocator allocator) {
		if(address == null) {
			address = allocator.allocate(JAVA_INT);
		}
		address.set(JAVA_INT, 0, value);
		return address;
	}

	/**
	 * Unmarshals this reference.
	 */
	private void unmarshal(MemorySegment address) {
		this.value = address.get(JAVA_INT, 0);
	}

	/**
	 * Sets this integer reference.
	 * @param value Integer reference
	 */
	void set(int value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(address);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof IntegerReference that) &&
				Objects.equals(this.address, that.address);
	}

	@Override
	public String toString() {
		return String.format("IntegerReference[%d]", value());
	}

	/**
	 * Native mapper for an integer reference.
	 */
	public static class IntegerReferenceMapper extends AbstractNativeMapper<IntegerReference, MemorySegment> {
		@Override
		public Class<IntegerReference> type() {
			return IntegerReference.class;
		}

		@Override
		public MemorySegment marshal(IntegerReference ref, SegmentAllocator allocator) {
			return ref.marshal(allocator);
		}

		@Override
		public MemorySegment marshalNull() {
			throw new NullPointerException();
		}

		@Override
		public BiConsumer<MemorySegment, IntegerReference> reference() {
			return (address, ref) -> ref.unmarshal(address);
		}
	}
}
