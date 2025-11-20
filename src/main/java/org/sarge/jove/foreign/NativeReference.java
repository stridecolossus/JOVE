package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.Objects;
import java.util.function.Function;

/**
 * A <i>native reference</i> is a template implementation for a <i>by reference</i> parameter with an <i>atomic</i> type, i.e. a primitive value or a simple pointer.
 * @param <T> Data type
 * @author Sarge
 */
public abstract class NativeReference<T> {
	private final AddressLayout layout;
	private MemorySegment pointer;
	private T value;

	/**
	 * Constructor.
	 * @param layout Memory layout of the underlying pointer
	 */
	protected NativeReference(AddressLayout layout) {
		this.layout = requireNonNull(layout);
	}

	/**
	 * @return Memory layout of the underlying pointer
	 */
	public AddressLayout layout() {
		return layout;
	}

	/**
	 * @return Referenced value or {@code null} if not populated
	 */
	public T get() {
		if((value == null) && (pointer != null)) {
			value = update(pointer, layout);
		}

		return value;
	}

	/**
	 * Updates the by-reference value from the given pointer.
	 * @param pointer 		Pointer
	 * @param layout		Memory layout
	 * @return Referenced value or {@code null} if not present
	 */
	protected abstract T update(MemorySegment pointer, AddressLayout layout);

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
			pointer = allocator.allocate(layout);
		}
		return pointer;
	}

	@Override
	public int hashCode() {
		return Objects.hash(layout, value);
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
		return String.format("NativeReference[layout=%s value=%s]", layout, value);
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
	}
}
