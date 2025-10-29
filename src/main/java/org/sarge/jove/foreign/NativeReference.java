package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.Objects;

/**
 * A <i>native reference</i> is a template implementation for a <i>by reference</i> parameter with an <i>atomic</i> type, i.e. a primitive value or a simple pointer.
 * @param <T> Data type
 * @see Returned
 * @author Sarge
 */
public abstract class NativeReference<T> {
	private T value;

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
	 * Updates the value of this reference.
	 * @param pointer Pointer
	 */
	protected abstract void update(MemorySegment pointer);

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
			return allocator.allocate(ValueLayout.ADDRESS);
		}

		@Override
		public NativeReference<?> unmarshal(MemorySegment address) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ReturnedTransformer<NativeReference<?>> update() {
			return (address, ref) -> ref.update(address);
		}
	}
}
