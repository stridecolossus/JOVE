package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.*;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractNativeMapper<T, R> implements NativeMapper<T, R> {
	@Override
	public MemoryLayout layout(Class<? extends T> type) {
		return AddressLayout.ADDRESS;
	}

	@Override
	public Object marshalNull(Class<? extends T> type) {
		return MemorySegment.NULL;
	}

	@Override
	public Function<R, T> returns(Class<? extends T> target) {
		throw new UnsupportedOperationException();
	}

	@Override
	public BiConsumer<R, T> unmarshal(Class<? extends T> target) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		return this.type().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof NativeMapper that) &&
				this.type().equals(that.type());
	}

	@Override
	public String toString() {
		return String.format("NativeMapper[%s]", this.type());
	}
}
