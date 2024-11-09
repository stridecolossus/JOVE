package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;

/**
 * An <i>integer reference</i> maps to a native integer-by-reference value.
 * @author Sarge
 */
public final class IntegerReference extends Address {
	/**
	 * Constructor.
	 * @param arena Arena
	 */
	public IntegerReference(Arena arena) {
		super(arena.allocate(ADDRESS));
	}

	/**
	 * @return Integer value
	 */
	public int value() {
		return this.address().get(JAVA_INT, 0);
	}

	/**
	 * Sets this integer reference.
	 * @param value Integer reference
	 */
	public void set(int value) {
		this.address().set(JAVA_INT, 0, value);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof IntegerReference that) &&
				(this.value() == that.value());
	}

	@Override
	public String toString() {
		return String.format("IntegerReference[%d]", value());
	}

	/**
	 * Native mapper for an integer-by-reference value.
	 */
	public static final class IntegerReferenceNativeMapper extends AddressNativeMapper<IntegerReference> {
		/**
		 * Constructor.
		 */
		public IntegerReferenceNativeMapper() {
			super(IntegerReference.class);
		}
	}
}
