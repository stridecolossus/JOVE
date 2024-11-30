package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.*;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractNativeTransformer<T, R> implements NativeTransformer<T, R> {
	@Override
	public NativeTransformer<? extends T, R> derive(Class<? extends T> target) {
		return this;
	}

	@Override
	public MemoryLayout layout() {
		return AddressLayout.ADDRESS;
	}

	@Override
	public Object empty() {
		return MemorySegment.NULL;
	}

	@Override
	public void transform(T instance, MemorySegment address, SegmentAllocator allocator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Function<R, T> returns() {
		throw new UnsupportedOperationException();
	}

	@Override
	public BiConsumer<R, T> update() {
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
				(obj instanceof NativeTransformer that) &&
				this.type().equals(that.type());
	}

	@Override
	public String toString() {
		return String.format("NativeTransformer[%s]", this.type());
	}
}
