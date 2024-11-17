package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.MemorySegment;

/**
 * An <i>integer reference</i> maps to a native integer-by-reference value.
 * @author Sarge
 */
public final class IntegerReference {
	private final Pointer pointer = new Pointer();

	/**
	 * @return Integer value
	 */
	public int value() {
		if(pointer.isAllocated()) {
			return pointer.address().get(JAVA_INT, 0);
		}
		else {
			return 0;
		}
	}

	/**
	 * Sets this integer reference.
	 * @param value Integer reference
	 */
	void set(int value) {
		pointer.address().set(JAVA_INT, 0, value);
	}

	@Override
	public int hashCode() {
		return pointer.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof IntegerReference that) &&
				this.pointer.equals(that.pointer);
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
			super(IntegerReference.class, JAVA_INT);
		}

		@Override
		public MemorySegment toNative(IntegerReference value, NativeContext context) {
			return value.pointer.allocate(JAVA_INT, context);
		}

		@Override
		public MemorySegment toNativeNull(Class<? extends IntegerReference> type) {
			throw new UnsupportedOperationException();
		}
	}
}
