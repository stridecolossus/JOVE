package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.Objects;
import java.util.function.Function;

import org.sarge.jove.common.Handle;

/**
 * A <i>native reference</i> models a <i>by reference</i> parameter with an <i>atomic</i> data type.
 * The native reference is updated as a side effect on invocation of the native method.
 * <p>
 * This implementation is intended to support <i>atomic</i> types such as primitives or immutable domain types.
 * The {@link Returned} annotation is a similar mechanism that supports more complex by-reference types such as structures and arrays.
 * <p>
 * The reference {@link Factory} is used to generate new native references as required by JOVE components.
 * <p>
 * @param <T> Reference type
 * @see Handle
 * @see Returned
 * @author Sarge
 */
public abstract class NativeReference<T> {
	protected MemorySegment pointer;

	/**
	 * @return Referenced value
	 */
	public abstract T get();

	/**
	 * Allocates the underlying pointer for this reference.
	 * @param allocator Off-heap allocator
	 * @return Reference pointer
	 */
	private MemorySegment allocate(SegmentAllocator allocator) {
		if(pointer == null) {
			pointer = allocator.allocate(ValueLayout.ADDRESS);
		}

		return pointer;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pointer);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof NativeReference that) &&
				Objects.equals(this.pointer, that.pointer);
	}

	@Override
	public String toString() {
		return String.format("NativeReference[%s]", pointer);
	}

	/**
	 * The <i>native reference factory</i> generates new native references on demand.
	 */
	public static class Factory {
		/**
		 * @return Integer-by-reference
		 */
		public NativeReference<Integer> integer() {
			return new NativeReference<>() {
				@Override
				public Integer get() {
					if(pointer == null) {
						return 0;
					}
					else {
						return pointer.get(ValueLayout.JAVA_INT, 0);
					}
				}
			};
		}

		/**
		 * @return Pointer-by-reference
		 */
		public NativeReference<Handle> pointer() {
			return new NativeReference<>() {
				@Override
				public Handle get() {
					if(pointer == null) {
						return null;
					}
					else {
						final MemorySegment address = pointer.get(ValueLayout.ADDRESS, 0);
						return new Handle(address);
					}
				}
			};
		}
	}

	/**
	 * Transformer for native references.
	 */
	public static class NativeReferenceTransformer implements Transformer<NativeReference<?>> {
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
