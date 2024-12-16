package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.Objects;
import java.util.function.*;

import org.sarge.jove.common.Handle;

/**
 * A <i>native reference</i> models a parameter that is returned <i>by reference</i> from a native method.
 * TODO
 * - implicitly @Returned
 * - get() after invocation
 * @param <T> Reference type
 * @see Returned
 * @author Sarge
 */
public abstract class NativeReference<T> {
	private MemorySegment address;

	/**
	 * Allocates the pointer for this reference as required.
	 * @param allocator Allocator
	 * @return Pointer
	 */
	private MemorySegment transform(SegmentAllocator allocator) {
		if(address == null) {
			address = allocator.allocate(ADDRESS);
		}

		return address;
	}

	/**
	 * Updates this reference <i>after</i> invocation of the native method.
	 * @param address Memory address
	 */
	protected abstract void update(MemorySegment address);

	/**
	 * @return Data contained by this reference
	 */
	public abstract T get();

	/**
	 * Factory for commonly used native reference types.
	 */
	public static class Factory {
    	/**
    	 * @return Integer-by-reference parameter
    	 */
    	public NativeReference<Integer> integer() {
    		return new NativeReference<>() {
    			private Integer value;

    			@Override
    			public Integer get() {
    				return Objects.requireNonNullElse(value, 0);
    			}

    			@Override
    			protected void update(MemorySegment address) {
    				value = address.get(ValueLayout.JAVA_INT, 0L);
    			}
    		};
    	}

    	/**
    	 * @return Pointer-by-reference parameter
    	 */
    	public NativeReference<Handle> pointer() {
    		return new NativeReference<>() {
    			private Handle handle;

    			@Override
    			public Handle get() {
    				return handle;
    			}

    			@Override
    			protected void update(MemorySegment address) {
    				handle = new Handle(address.get(ADDRESS, 0L));
    			}
    		};
    	}
	}

	/**
	 * Transformer for a native reference.
	 */
	@SuppressWarnings("rawtypes")
	public record NativeReferenceTransformer() implements NativeTransformer<NativeReference, MemorySegment> {
//		@Override
//		public Object empty() {
//			throw new NullPointerException("A native reference cannot be NULL");
//		}

		@Override
		public MemorySegment transform(NativeReference ref, ParameterMode parameter, SegmentAllocator allocator) {
			requireNonNull(ref);
			return ref.transform(allocator);
		}

		@Override
		public Function<MemorySegment, NativeReference> returns() {
			throw new UnsupportedOperationException();
		}

		@Override
		public BiConsumer<MemorySegment, NativeReference> update() {
			return (address, ref) -> ref.update(address);
		}
	}
}
