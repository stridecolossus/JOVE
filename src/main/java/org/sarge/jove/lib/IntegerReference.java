package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

/**
 * An <i>integer reference</i> maps a mutable integer to a native integer-by-reference.
 * @author Sarge
 */
public class IntegerReference {
	private final MemorySegment address;

	public IntegerReference(Arena arena) {
		this.address = arena.allocate(JAVA_INT);
	}

	public int value() {
		return address.get(JAVA_INT, 0);
	}

	@Override
	public int hashCode() {
		return address.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof IntegerReference that) &&
				(this.value() == that.value());
	}

	/**
	 * Native mapper for an integer reference.
	 */
	public static class IntegerReferenceNativeMapper extends DefaultNativeMapper implements NativeTypeConverter<IntegerReference, MemorySegment> {
		/**
		 * Constructor.
		 */
		public IntegerReferenceNativeMapper() {
			super(IntegerReference.class, ValueLayout.ADDRESS);
		}

		@Override
		public MemorySegment toNative(IntegerReference ref, Class<?> type, Arena arena) {
			return ref.address;
		}

		@Override
		public IntegerReference fromNative(MemorySegment value, Class<?> type) {
			throw new UnsupportedOperationException();
		}
	}
}
