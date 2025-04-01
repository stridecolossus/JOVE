package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

import org.sarge.jove.common.Handle;

/**
 * A <i>native reference</i> models a <i>by reference</i> parameter returned as a side-effect from a native method.
 * @param <T> Reference type
 * @author Sarge
 */
public abstract class NativeReference<T> {
	protected final MemorySegment pointer;

	protected NativeReference() {
		this(ValueLayout.ADDRESS);
	}

	protected NativeReference(MemoryLayout layout) {
		@SuppressWarnings("resource")
		final var allocator = Arena.ofAuto();
		this.pointer = allocator.allocate(layout);
	}

	/**
	 * @return Referenced value
	 */
	public abstract T get();

	@Override
	public String toString() {
		return pointer.toString();
	}

	/**
	 * Factory for commonly used native reference types.
	 */
	public static class Factory {
		/**
		 * @return New integer-by-reference
		 */
		public NativeReference<Integer> integer() {
			return new NativeReference<>() {
				@Override
				public Integer get() {
					return super.pointer.get(ValueLayout.JAVA_INT, 0L);
				}
			};
		}

		/**
		 * @return New pointer-by-reference
		 */
		public NativeReference<Handle> pointer() {
			return new NativeReference<>() {
				@Override
				public Handle get() {
					final MemorySegment address = super.pointer.get(ValueLayout.ADDRESS, 0L);
					if(MemorySegment.NULL.equals(address)) {
						return null;
					}
					else {
						return new Handle(address);
					}
				}
			};
		}
	}

	/**
	 * Native transformer for by-reference types.
	 */
	@SuppressWarnings("rawtypes")
	public static class NativeReferenceTransformer implements ReferenceTransformer<NativeReference> {
		@Override
		public Object marshal(NativeReference ref, SegmentAllocator allocator) {
			return ref.pointer;
		}

		@Override
		public Function<? extends Object, NativeReference> unmarshal() {
			throw new UnsupportedOperationException();
		}
	}
}
