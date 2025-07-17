package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.Objects;

import org.sarge.jove.common.Handle;

/**
 * A <i>native reference</i> models a <i>by reference</i> parameter with an <i>atomic</i> data type.
 * The native reference is updated as a side effect on invocation of the native method.
 * <p>
 * The {@link IntegerReference} and {@link Pointer} implementations support the common cases of by-reference integers and pointers.
 * <p>
 * This implementation is intended to support <i>atomic</i> types such as primitives or immutable domain types.
 * The {@link Returned} annotation is a similar mechanism that supports more complex by-reference types such as structures and arrays.
 * <p>
 * @param <T> Reference type
 * @see Returned
 * @author Sarge
 */
public abstract class NativeReference<T> {
	private T value;
	private MemorySegment pointer;

	/**
	 * @return Referenced value or {@code null} if not populated
	 */
	public T get() {
		if((value == null) && (pointer != null)) {
			value = update(pointer);
		}

		return value;
	}

	/**
	 * Explicitly sets this reference.
	 * @param value Referenced value
	 */
	public void set(T value) {
		this.value = value;
	}

	/**
	 * Allocates the underlying pointer.
	 * @param allocator Off-heap allocator
	 * @return Pointer
	 */
	private MemorySegment allocate(SegmentAllocator allocator) {
		if(pointer == null) {
			pointer = allocator.allocate(ValueLayout.ADDRESS);
		}

		return pointer;
	}

	/**
	 * Updates the value of this reference.
	 * @param pointer Pointer
	 * @return Value
	 */
	protected abstract T update(MemorySegment pointer);

	@Override
	public int hashCode() {
		return Objects.hash(value, pointer);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof NativeReference that) &&
				Objects.equals(this.value, that.value) &&
				Objects.equals(this.pointer, that.pointer);
	}

	@Override
	public String toString() {
		return String.format("NativeReference[pointer=%s, value=%s]", pointer, value);
	}

	/**
	 * Convenience integer-by-reference implementation.
	 */
	public static class IntegerReference extends NativeReference<Integer> {
		@Override
		protected Integer update(MemorySegment pointer) {
			return pointer.get(ValueLayout.JAVA_INT, 0L);
		}
	}

	/**
	 * A <i>pointer</i> is a reference to an off-heap address.
	 */
	public static class Pointer extends NativeReference<Handle> {
		@Override
		protected Handle update(MemorySegment pointer) {
			final MemorySegment address = pointer.get(ValueLayout.ADDRESS, 0L);
			// TODO - check for NULL?
			return new Handle(address);
		}
	}

	/**
	 * Transformer for native references.
	 */
	public static class NativeReferenceTransformer extends DefaultTransformer<NativeReference<?>> {
		@Override
		public final boolean isReference() {
			return false;
		}

		@Override
		public MemorySegment marshal(NativeReference<?> ref, SegmentAllocator allocator) {
			return ref.allocate(allocator);
		}
	}
}
