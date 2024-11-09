package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;

/**
 * An <i>address</i> is a base-class for a native type that encapsulates a {@link MemorySegment} pointer.
 * @author Sarge
 */
public abstract class Address {
	private final MemorySegment address;

	/**
	 * Constructor.
	 * @param address Memory address
	 */
	protected Address(MemorySegment address) {
		this.address = requireNonNull(address);
	}

	/**
	 * @return Memory address
	 */
	protected final MemorySegment address() {
		return address;
	}

	@Override
	public int hashCode() {
		return address.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Address that) &&
				this.address.equals(that.address());
	}

	/**
	 * Skeleton implementation.
	 * @param <T> Address type
	 */
	protected static class AddressNativeMapper<T extends Address> extends AbstractNativeMapper<T> {
		/**
		 * Constructor.
		 * @param type Domain type
		 */
		protected AddressNativeMapper(Class<T> type) {
			super(type, ValueLayout.ADDRESS);
		}

		@Override
		public MemorySegment toNative(T value, Arena arena) {
			return value.address().asReadOnly();
		}
	}
}
