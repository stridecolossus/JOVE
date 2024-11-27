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
	 * Initialises this reference before transformation.
	 */
	private MemorySegment init(SegmentAllocator allocator) {
		if(address == null) {
			address = allocator.allocate(JAVA_INT);
		}
		address.set(JAVA_INT, 0, value);
		return address;
	}

	/**
	 * Updates this reference after invocation.
	 */
	private void update(MemorySegment address) {
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
	 * Native transform for an integer reference.
	 */
	public static class IntegerReferenceTransform extends AbstractNativeTransformer<IntegerReference, MemorySegment> {
		@Override
		public Class<IntegerReference> type() {
			return IntegerReference.class;
		}

		@Override
		public MemorySegment transform(IntegerReference ref, SegmentAllocator allocator) {
			return ref.init(allocator);
		}

		@Override
		public MemorySegment empty() {
			throw new NullPointerException("A by-reference integer cannot be null");
		}

		@Override
		public BiConsumer<MemorySegment, IntegerReference> update() {
			return (address, ref) -> ref.update(address);
		}
	}
}
