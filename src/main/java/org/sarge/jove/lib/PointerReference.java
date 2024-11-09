package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.Arena;

/**
 * A <i>pointer reference</i> maps to a native pointer-by-reference.
 * @author Sarge
 */
public final class PointerReference extends Address {
	/**
	 * Constructor.
	 * @param arena Arena
	 */
	public PointerReference(Arena arena) {
		super(arena.allocate(ADDRESS));
	}

	/**
	 * @return This reference as an opaque handle
	 */
	public Handle handle() {
		return new Handle(this.address());
	}

	/**
	 * Native mapper for an pointer -by-reference value.
	 */
	public static final class PointerReferenceNativeMapper extends AddressNativeMapper<PointerReference> {
		/**
		 * Constructor.
		 */
		public PointerReferenceNativeMapper() {
			super(PointerReference.class);
		}
	}
}
