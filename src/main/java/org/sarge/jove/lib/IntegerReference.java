package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * An <i>integer reference</i> maps to a native integer-by-reference value.
 * @author Sarge
 */
public final class IntegerReference {
	private MemorySegment address;

	/**
	 * @return Integer value
	 */
	public int value() {
		if(address == null) {
			return 0;
		}
		else {
			return address.get(JAVA_INT, 0);
		}
	}

	/**
	 * Sets this integer reference.
	 * @param value Integer reference
	 */
	void set(int value) {
		address.set(JAVA_INT, 0, value);
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
	 * Native mapper for an integer-by-reference value.
	 */
	public static final class IntegerReferenceNativeMapper extends AbstractNativeMapper<IntegerReference> {
		/**
		 * Constructor.
		 */
		public IntegerReferenceNativeMapper() {
			super(IntegerReference.class);
		}

		@Override
		public MemorySegment marshal(IntegerReference ref, NativeContext context) {
			if(ref.address == null) {
				ref.address = context.allocator().allocate(JAVA_INT);
			}
			return ref.address;
		}

		@Override
		public MemorySegment marshalNull(Class<? extends IntegerReference> type) {
			throw new UnsupportedOperationException();
		}
	}
}
