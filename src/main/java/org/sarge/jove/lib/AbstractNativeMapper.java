package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractNativeMapper<T> implements NativeMapper<T> {
	private final Class<T> type;

	/**
	 * Constructor.
	 * @param type Java type
	 */
	protected AbstractNativeMapper(Class<T> type) {
		this.type = requireNonNull(type);
	}

	@Override
	public final Class<T> type() {
		return type;
	}

	@Override
	public MemoryLayout layout(Class<? extends T> type) {
		return AddressLayout.ADDRESS;
	}

	@Override
	public Object marshalNull(Class<? extends T> type) {
		return MemorySegment.NULL;
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof NativeMapper that) &&
				this.type.equals(that.type());
	}

	@Override
	public String toString() {
		return String.format("NativeMapper[%s]", type);
	}
}
