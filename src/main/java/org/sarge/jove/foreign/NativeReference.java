package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.Objects;
import java.util.function.*;

/**
 * A <i>native reference</i> is a template implementation for a <i>by reference</i> parameter with an <i>atomic</i> type, i.e. a primitive value or a simple pointer.
 * @param <T> Data type
 * @author Sarge
 */
public abstract class NativeReference<T> {
	private T value;
	private MemorySegment pointer;

	/**
	 * @return Referenced value or {@code null} if not populated
	 */
	public T get() {
		return value;
	}

	/**
	 * Sets this reference.
	 * @param value New value
	 */
	public void set(T value) {
		this.value = value;
	}

	/**
	 * Allocates this reference.
	 * @param allocator Allocator
	 * @return Pointer
	 */
	private MemorySegment allocate(SegmentAllocator allocator) {
		if(pointer == null) {
			pointer = allocator.allocate(ValueLayout.ADDRESS);
		}
		return pointer;
	}

	/**
	 * Extracts the by-reference value from the given pointer.
	 * @param pointer Pointer
	 * @return Referenced value
	 */
	protected abstract T update(MemorySegment pointer);

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof NativeReference that) &&
				Objects.equals(this.value, that.value);
	}

	@Override
	public String toString() {
		return String.format("NativeReference[%s]", value);
	}

	/**
	 * Transformer for native references.
	 */
	public static class NativeReferenceTransformer implements Transformer<NativeReference<?>, MemorySegment> {
		@Override
		public MemorySegment marshal(NativeReference<?> ref, SegmentAllocator allocator) {
			return ref.allocate(allocator);
		}

		@Override
		public Function<MemorySegment, NativeReference<?>> unmarshal() {
			throw new UnsupportedOperationException();
		}

		@Override
		public BiConsumer<MemorySegment, NativeReference<?>> update() {
			return new BiConsumer<>() {
				@SuppressWarnings({"rawtypes", "unchecked"})
				@Override
				public void accept(MemorySegment address, NativeReference reference) {
					final Object value = reference.update(address);
					reference.set(value);
				}
			};
		}
	}
}
