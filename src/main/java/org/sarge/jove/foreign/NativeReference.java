package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.util.Objects;

import org.sarge.jove.common.Handle;

/**
 * A <i>native reference</i> models a <i>by reference</i> parameter returned as a side-effect from a native method.
 * @param <T> Reference type
 * @author Sarge
 */
public abstract class NativeReference<T> {
	private T value;
	private MemorySegment pointer;

	/**
	 * Allocates the memory address of this reference.
	 * @param allocator Allocator
	 * @return Address
	 */
	private MemorySegment allocate(SegmentAllocator allocator) {
		if(pointer == null) {
			pointer = allocator.allocate(ValueLayout.ADDRESS);
		}

		return pointer;
	}

	/**
	 * @return Referenced value
	 */
	public T get() {
		if(Objects.nonNull(pointer)) {
			value = update(pointer);
		}

		return value;
	}

	/**
	 * Updates the value after invocation.
	 * @param address Memory address of this reference
	 * @return Value
	 */
	protected abstract T update(MemorySegment address);

	/**
	 * Sets the value of this reference.
	 * @param value New value
	 */
	public void set(T value) {
		this.value = requireNonNull(value);
	}

	@Override
	public String toString() {
		return String.format("NativeReference[ptr=%s value=%s]", pointer, value);
	}

	/**
	 * Creates a native reference for a primitive with the given layout.
	 * @param <P> Primitive type
	 * @param layout Primitive layout
	 * @return Primitive native reference
	 */
	public static <P> NativeReference<P> reference(ValueLayout layout) {
		return new NativeReference<>() {
			private final VarHandle handle = layout.varHandle();

			@Override
			protected P update(MemorySegment address) {
				return (P) handle.get(address, 0L);
			}
		};
	}
	// TODO - actually restrict to primitives? layout.carrier().isPrimitive()

	/**
	 * Factory for commonly used native reference types.
	 */
	public static class Factory {
		/**
		 * Creates a by-reference integer.
		 * @return Integer by-reference
		 */
		public NativeReference<Integer> integer() {
			final NativeReference<Integer> ref = reference(ValueLayout.JAVA_INT);
			ref.set(0);
			return ref;
		}

		/**
		 * Creates a by-reference pointer.
		 * @return By-reference handle/pointer
		 */
		public NativeReference<Handle> pointer() {
			return Handle.reference();
		}
	}

	/**
	 * Native transformer for by-reference types.
	 */
	public static class NativeReferenceTransformer implements AddressTransformer<NativeReference<?>, MemorySegment> {
		@Override
		public MemoryLayout layout() {
			return ValueLayout.ADDRESS;
		}

		@Override
		public MemorySegment marshal(NativeReference<?> ref, SegmentAllocator allocator) {
			return ref.allocate(allocator);
		}

		@Override
		public Object unmarshal(MemorySegment value) {
			throw new UnsupportedOperationException();
		}
	}
}
